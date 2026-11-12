package com.bank.banking_platform.auth.service;

import com.bank.banking_platform.auth.dto.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    private static final int DEFAULT_LIMIT = 100; // requests per minute
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(1);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public RateLimiterService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Rate limit kontrolü yapar. Redis'te dağıtık sayaç kullanır.
     *
     * @param request HTTP request for IP extraction
     * @param endpointKey endpoint identifier (e.g., "login", "register", "refresh", "general")
     * @return true if request allowed, false if rate limit exceeded
     */
    public boolean checkRateLimit(HttpServletRequest request, String endpointKey) {
        String ip = getClientIp(request);
        String key = RATE_LIMIT_KEY_PREFIX + endpointKey + ":" + ip;

        // Redis'de sayacı artır
        Long count = stringRedisTemplate.opsForValue().increment(key);

        if (count == 1) {
            // İlk istek: TTL ayarla (1 dakika)
            stringRedisTemplate.expire(key, 60, TimeUnit.SECONDS);
        }

        // Limit kontrolü - endpoint'e göre farklı limitler
        int limit = getLimitForEndpoint(endpointKey);

        if (count > limit) {
            // Rate limit aşıldı - sayacı tamamlama (reset için)
            // Next retry after hesapla
            Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttl == null) {
                ttl = 60L; // fallback
            }
            // Count'i tutarlı tutmak için bir sonraki istekte sıfırlama
            // (gerçek production'da daha subtle bir approach kullanılmalı)
            return false;
        }

        return true;
    }

    /**
     * Endpoint'e göre rate limit belirle
     */
    private int getLimitForEndpoint(String endpointKey) {
        return switch (endpointKey) {
            case "login" -> 5;
            case "register" -> 3;
            case "refresh" -> 10;
            case "general" -> 100;
            default -> 100;
        };
    }

    /**
     * İstemci IP'sini çıkar (Proxy'leri dikkate al)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For'den ilk IP'yi al (gerçek istemci)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * Rate limit aşıldığında 429 yanıtı gönder
     */
    public void sendRateLimitResponse(HttpServletResponse response, HttpServletRequest request) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");

        ApiError error = ApiError.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.TOO_MANY_REQUESTS.value())
            .error(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())
            .message("Çok fazla istek gönderildi. Lütfen " + getRetryAfter(request) + " saniye sonra tekrar deneyin.")
            .path(request.getRequestURI())
            .build();

        response.getWriter().write(objectMapper.writeValueAsString(error));
        response.getWriter().flush();
    }

    /**
     * Retry-After header değerini hesapla
     */
    private int getRetryAfter(HttpServletRequest request) {
        // Basit bir backoff: mevver sayaç TTL'ine bağlı
        // Gerçek uygulamada Redis key'in expire'ine bakılır
        return 60; // 1 dakika boyunca wait
    }
}
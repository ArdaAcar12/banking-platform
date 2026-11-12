package com.bank.banking_platform.auth.security;

import com.bank.banking_platform.auth.dto.ApiError;
import com.bank.banking_platform.auth.service.RateLimiterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String[] RATE_LIMIT_ENDPOINTS = {
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/refresh"
    };

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    @Autowired
    public RateLimitFilter(RateLimiterService rateLimiterService, ObjectMapper objectMapper) {
        this.rateLimiterService = rateLimiterService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Sadece authentication endpoint'leri için rate limiting uygula
        boolean isRateLimitedEndpoint = false;
        for (String endpoint : RATE_LIMIT_ENDPOINTS) {
            if (requestURI.startsWith(endpoint)) {
                isRateLimitedEndpoint = true;
                break;
            }
        }

        if (isRateLimitedEndpoint) {
            if (!rateLimiterService.checkRateLimit(request, extractEndpointKey(requestURI))) {
                rateLimiterService.sendRateLimitResponse(response, request);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractEndpointKey(String requestURI) {
        if (requestURI.contains("/login")) return "login";
        if (requestURI.contains("/register")) return "register";
        if (requestURI.contains("/refresh")) return "refresh";
        return "general";
    }
}
package com.bank.banking_platform.auth.service;

import com.bank.banking_platform.auth.entity.RefreshToken;
import com.bank.banking_platform.auth.entity.User;
import com.bank.banking_platform.auth.exception.InvalidRefreshTokenException;
import com.bank.banking_platform.auth.repository.RefreshTokenRepository;
import com.bank.banking_platform.auth.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Refresh Token işlemlerini güvenli bir şekilde yönetir.
 * <p>
 * Güvenlik kuralları:
 * <ul>
 *   <li>Token veritabanında açık metin olarak saklanmaz; SHA-256 hash'i saklanır.</li>
 *   <li>Token yenilendiğinde eski token iptal edilir ve yeni token üretilir (rotation).</li>
 *   <li>Token tekrar kullanımı tespit edildiğinde kullanıcının tüm oturumları iptal edilir.</li>
 *   <li>Tüm işlemler atomik transaction içinde gerçekleştirilir.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh.token.expiration-seconds:604800}")
    private long refreshExpirationSeconds;

    /**
     * Kullanıcı için yeni bir Refresh Token üretir ve hash'ini veritabanına kaydeder.
     * Açık metin token çağrıya geri döndürülür (sadece bir kez).
     */
    @Transactional
    public RefreshTokenPair createRefreshToken(User user) {
        String plainToken = jwtService.generateRefreshToken();
        String tokenHash = hashToken(plainToken);
        Instant expiryDate = Instant.now().plusSeconds(refreshExpirationSeconds);

        RefreshToken entity = RefreshToken.builder()
            .user(user)
            .tokenHash(tokenHash)
            .expiryDate(expiryDate)
            .revoked(false)
            .build();

        RefreshToken saved = refreshTokenRepository.save(entity);
        return new RefreshTokenPair(saved, plainToken);
    }

    /**
     * Refresh Token'ı doğrular, rotasyon uygular ve yeni bir çift (access + refresh) döndürür.
     * Eski token iptal edilir; yeni token üretilir.
     * Token tekrar kullanımı tespit edilirse kullanıcının tüm token'ları iptal edilir.
     */
    @Transactional
    public RefreshTokenPair rotateRefreshToken(String presentedToken) {
        String tokenHash = hashToken(presentedToken);

        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new InvalidRefreshTokenException("Geçersiz refresh token."));

        User user = token.getUser();

        // Token zaten iptal edilmişse: tekrar kullanım (token çalınabilirliği) tespit edildi.
        if (token.isRevoked()) {
            revokeAllByUser(user);
            throw new InvalidRefreshTokenException(
                "Refresh token iptal edilmiş veya tekrar kullanılmıştır. Tüm oturumlarınız güvenlik nedeniyle iptal edildi.");
        }

        // Token süresi dolmuşsa iptal edilir.
        if (token.isExpired()) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new InvalidRefreshTokenException("Refresh token süresi dolmuş.");
        }

        // Eski token iptal edilir ve yeni token üretilir (rotation).
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return createRefreshToken(user);
    }

    /**
     * Belirtilen Refresh Token'ı iptal eder (çıkış işlemi).
     */
    @Transactional
    public void revokeToken(String plainToken, User user) {
        String tokenHash = hashToken(plainToken);
        RefreshToken token = refreshTokenRepository.findByTokenHashAndUser(tokenHash, user)
            .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token bulunamadı."));
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    /**
     * Kullanıcıya ait tüm Refresh Token'ları iptal eder.
     */
    @Transactional
    public void revokeAllByUser(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUser(user);
        for (RefreshToken token : tokens) {
            token.setRevoked(true);
        }
        refreshTokenRepository.saveAll(tokens);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algoritması desteklenmiyor.", ex);
        }
    }

    /**
     * Refresh Token entity'si ile birlikte tek seferlik açık metin değeri.
     */
    public record RefreshTokenPair(RefreshToken refreshToken, String plainToken) {}
}

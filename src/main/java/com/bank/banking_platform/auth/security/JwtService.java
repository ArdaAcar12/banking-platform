package com.bank.banking_platform.auth.security;

import com.bank.banking_platform.auth.entity.User;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * JWT Access Token işlemlerini yönetir.
 * <p>
 * İmzalama ve doğrulama Nimbus JOSE JWT üzerinden HMAC SHA-256 ile yapılır.
 * <p>
 * Güvenlik kuralları:
 * <ul>
 *   <li>Token imzalama anahtarı environment variable'dan ({@code JWT_SECRET}) gelir, kodda sabit değildir.</li>
 *   <li>Anahtar en az 32 karakter (HS256 için 256 bit) olmalıdır; aksi halde uygulama başlatılamaz.</li>
 *   <li>Access Token kısa ömürlüdür (varsayılan 15 dakika).</li>
 *   <li>Claim'ler: subject (user UUID), username, role, issued at, expiration, issuer.</li>
 * </ul>
 */
@Service
public class JwtService {

    private static final int MIN_SECRET_LENGTH = 32;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer:banking-platform}")
    private String issuer;

    @Value("${jwt.access.token.expiration-seconds:900}")
    private long accessExpirationSeconds;

    @PostConstruct
    public void init() {
        validateSecret(secret);
    }

    private void validateSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                "JWT_SECRET is not set. A secure JWT signing secret must be provided via the JWT_SECRET environment variable.");
        }
        if (secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                "JWT_SECRET is too short (minimum " + MIN_SECRET_LENGTH + " characters required for HS256).");
        }
        if ("change-this-to-a-strong-secret".equals(secret)) {
            throw new IllegalStateException(
                "JWT_SECRET is using the insecure default placeholder. Please set a strong, unique JWT_SECRET.");
        }
    }

    private SecretKey secretKey() {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String generateAccessToken(User user) {
        try {
            Date now = new Date();
            Date expiry = new Date(now.getTime() + accessExpirationSeconds * 1000);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .issueTime(now)
                .expirationTime(expiry)
                .claim("username", user.getUsername())
                .claim("role", user.getRole().name())
                .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJWT.sign(new MACSigner(secretKey()));
            return signedJWT.serialize();
        } catch (JOSEException ex) {
            throw new JwtException("Access token imzalanamadı.", ex);
        }
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        try {
            Date now = new Date();
            Date expiry = new Date(now.getTime() + accessExpirationSeconds * 1000);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(userDetails.getId().toString())
                .issueTime(now)
                .expirationTime(expiry)
                .claim("username", userDetails.getUsername())
                .claim("role", userDetails.getRole().name())
                .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJWT.sign(new MACSigner(secretKey()));
            return signedJWT.serialize();
        } catch (JOSEException ex) {
            throw new JwtException("Access token imzalanamadı.", ex);
        }
    }

    /**
     * Token'ı çözer ve geçerliliğini (imza, süresi, yayıncı) kontrol eder.
     * Geçersiz, süresi dolmuş veya hatalı imzalı token'larda {@code JwtException} fırlatır.
     */
    public JWTClaimsSet decode(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            if (!signedJWT.verify(new MACVerifier(secretKey()))) {
                throw new JwtException("Token imzası geçersiz.");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            if (!issuer.equals(claims.getIssuer())) {
                throw new JwtException("Token yayıncı bilgisi geçersiz.");
            }

            Date expiration = claims.getExpirationTime();
            if (expiration != null && expiration.before(new Date())) {
                throw new JwtException("Token süresi dolmuş.");
            }

            return claims;
        } catch (JOSEException | ParseException ex) {
            throw new JwtException("Token çözümlenemedi.", ex);
        }
    }

    public String extractUsername(String token) {
        try {
            return decode(token).getStringClaim("username");
        } catch (ParseException ex) {
            throw new JwtException("Kullanıcı adı talebi okunamadı.", ex);
        }
    }

    public String extractRole(String token) {
        try {
            return decode(token).getStringClaim("role");
        } catch (ParseException ex) {
            throw new JwtException("Rol talebi okunamadı.", ex);
        }
    }

    public UUID extractUserId(String token) {
        try {
            return UUID.fromString(decode(token).getSubject());
        } catch (IllegalArgumentException ex) {
            throw new JwtException("Kullanıcı kimliği talebi okunamadı.", ex);
        }
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationSeconds;
    }

    /**
     * Güvenli rastgele bir Refresh Token değeri üretir.
     */
    public String generateRefreshToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

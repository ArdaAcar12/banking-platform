package com.bank.banking_platform.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bank.banking_platform.auth.entity.Role;
import com.bank.banking_platform.auth.entity.User;
import com.bank.banking_platform.auth.security.JwtService;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * JwtService unit testleri. Spring konteksi olmadan doğrudan test edilir.
 */
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret",
            "test-secret-key-for-unit-tests-1234567890"); // 39 karakter, HS256 için yeterli
        ReflectionTestUtils.setField(jwtService, "issuer", "test-issuer");
        ReflectionTestUtils.setField(jwtService, "accessExpirationSeconds", 900L);
        jwtService.init();
    }

    private User testUser() {
        return User.builder()
            .id(UUID.randomUUID())
            .email("user@example.com")
            .username("testuser")
            .passwordHash("hashed")
            .firstName("Test")
            .lastName("User")
            .role(Role.CUSTOMER)
            .enabled(true)
            .accountNonLocked(true)
            .build();
    }

    @Test
    void shouldGenerateAccessTokenWithExpectedClaims() {
        User user = testUser();

        String token = jwtService.generateAccessToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUserId(token)).isEqualTo(user.getId());
        assertThat(jwtService.extractUsername(token)).isEqualTo(user.getUsername());
        assertThat(jwtService.extractRole(token)).isEqualTo(Role.CUSTOMER.name());
    }

    @Test
    void shouldDecodeValidToken() {
        User user = testUser();
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.decode(token).getSubject()).isEqualTo(user.getId().toString());
        assertThat(jwtService.decode(token).getIssuer()).isEqualTo("test-issuer");
        assertThat(jwtService.decode(token).getExpirationTime()).isAfter(new Date());
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecret() {
        User user = testUser();
        String token = jwtService.generateAccessToken(user);

        JwtService other = new JwtService();
        ReflectionTestUtils.setField(other, "secret", "a-completely-different-secret-key-1234567890");
        ReflectionTestUtils.setField(other, "issuer", "other");
        ReflectionTestUtils.setField(other, "accessExpirationSeconds", 900L);
        other.init();

        assertThatThrownBy(() -> other.decode(token))
            .isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectExpiredToken() {
        User user = testUser();
        JwtService expiredService = new JwtService();
        ReflectionTestUtils.setField(expiredService, "secret",
            "test-secret-key-for-unit-tests-1234567890");
        ReflectionTestUtils.setField(expiredService, "issuer", "test-issuer");
        // Negatif süre sonu: token üretildiği anda zaten süresi dolmuş olur.
        ReflectionTestUtils.setField(expiredService, "accessExpirationSeconds", -1L);
        expiredService.init();

        String token = expiredService.generateAccessToken(user);

        assertThatThrownBy(() -> expiredService.decode(token))
            .isInstanceOf(JwtException.class);
    }
}

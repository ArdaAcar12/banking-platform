package com.bank.banking_platform.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bank.banking_platform.auth.entity.RefreshToken;
import com.bank.banking_platform.auth.entity.User;
import com.bank.banking_platform.auth.entity.Role;
import com.bank.banking_platform.auth.exception.InvalidRefreshTokenException;
import com.bank.banking_platform.auth.repository.RefreshTokenRepository;
import com.bank.banking_platform.auth.security.JwtService;
import com.bank.banking_platform.auth.service.RefreshTokenService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * RefreshTokenService unit testleri. Döndürme (rotation), iptal ve tekrar kullanım tespiti
 * senaryolarını kapsar. Şifreleme SHA-256 ile içte yapıldığı zaman repository eşleştirmeleri
 * {@code anyString()} ile yapılır.
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RefreshTokenService refreshTokenService;
    private User user;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, jwtService, passwordEncoder);
        user = User.builder().id(UUID.randomUUID()).email("user@example.com")
            .username("testuser").passwordHash("hashed").role(Role.CUSTOMER).enabled(true)
            .accountNonLocked(true).build();
    }

    @Test
    void shouldCreateRefreshTokenAndReturnPlaintextOnce() {
        when(jwtService.generateRefreshToken()).thenReturn("plain-refresh-token");
        RefreshToken saved = RefreshToken.builder().id(UUID.randomUUID()).user(user)
            .tokenHash("stored-hash").expiryDate(Instant.now().plusSeconds(3600)).revoked(false).build();
        when(refreshTokenRepository.save(any())).thenReturn(saved);

        RefreshTokenService.RefreshTokenPair pair = refreshTokenService.createRefreshToken(user);

        assertThat(pair.plainToken()).isEqualTo("plain-refresh-token");
        assertThat(pair.refreshToken().getTokenHash()).isEqualTo("stored-hash");
        assertThat(pair.refreshToken().isRevoked()).isFalse();
        assertThat(pair.refreshToken().getUser()).isEqualTo(user);
    }

    @Test
    void shouldRotateValidRefreshToken() {
        String presented = "presented-token";
        RefreshToken oldToken = RefreshToken.builder().id(UUID.randomUUID()).user(user)
            .tokenHash("any-hash").expiryDate(Instant.now().plusSeconds(3600)).revoked(false).build();

        // SHA-256 hash içte hesaplandığından repository eşlemesi herhangi bir hash ile yapılır.
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(java.util.Optional.of(oldToken));
        when(jwtService.generateRefreshToken()).thenReturn("new-plain-token");
        RefreshToken newToken = RefreshToken.builder().id(UUID.randomUUID()).user(user)
            .tokenHash("new-stored-hash").expiryDate(Instant.now().plusSeconds(3600)).revoked(false).build();
        when(refreshTokenRepository.save(any())).thenReturn(oldToken).thenReturn(newToken);

        RefreshTokenService.RefreshTokenPair pair = refreshTokenService.rotateRefreshToken(presented);

        assertThat(pair.plainToken()).isEqualTo("new-plain-token");
        verify(refreshTokenRepository).save(eq(oldToken));
        assertThat(oldToken.isRevoked()).isTrue();
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        String presented = "expired-token";
        RefreshToken expiredToken = RefreshToken.builder().id(UUID.randomUUID()).user(user)
            .tokenHash("any-hash").expiryDate(Instant.now().minusSeconds(1)).revoked(false).build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(java.util.Optional.of(expiredToken));

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken(presented))
            .isInstanceOf(InvalidRefreshTokenException.class)
            .hasMessageContaining("süresi dolmuş");
    }

    @Test
    void shouldRejectRevokedRefreshToken() {
        String presented = "revoked-token";
        RefreshToken revokedToken = RefreshToken.builder().id(UUID.randomUUID()).user(user)
            .tokenHash("any-hash").expiryDate(Instant.now().plusSeconds(3600)).revoked(true).build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(java.util.Optional.of(revokedToken));
        when(refreshTokenRepository.findByUser(user)).thenReturn(List.of(revokedToken));

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken(presented))
            .isInstanceOf(InvalidRefreshTokenException.class)
            .hasMessageContaining("iptal edilmiş");

        // Token tekrar kullanımı tespit edildi: kullanıcının tüm token'ları iptal edilmeli.
        verify(refreshTokenRepository).saveAll(List.of(revokedToken));
    }

    @Test
    void shouldRejectUnknownRefreshToken() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("unknown-token"))
            .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void shouldRevokeTokenOnLogout() {
        String presented = "refresh-token";
        RefreshToken token = RefreshToken.builder().id(UUID.randomUUID()).user(user)
            .tokenHash("any-hash").expiryDate(Instant.now().plusSeconds(3600)).revoked(false).build();

        when(refreshTokenRepository.findByTokenHashAndUser(anyString(), eq(user)))
            .thenReturn(java.util.Optional.of(token));

        refreshTokenService.revokeToken(presented, user);

        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void shouldThrowWhenRevokingUnknownToken() {
        when(refreshTokenRepository.findByTokenHashAndUser(anyString(), any()))
            .thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.revokeToken("unknown", user))
            .isInstanceOf(InvalidRefreshTokenException.class);
    }
}

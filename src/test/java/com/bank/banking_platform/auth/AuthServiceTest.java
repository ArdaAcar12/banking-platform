package com.bank.banking_platform.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bank.banking_platform.auth.dto.AuthResponse;
import com.bank.banking_platform.auth.dto.LoginRequest;
import com.bank.banking_platform.auth.dto.RegisterRequest;
import com.bank.banking_platform.auth.dto.UserResponse;
import com.bank.banking_platform.auth.entity.RefreshToken;
import com.bank.banking_platform.auth.entity.Role;
import com.bank.banking_platform.auth.entity.User;
import com.bank.banking_platform.auth.exception.AuthenticationException;
import com.bank.banking_platform.auth.exception.DuplicateResourceException;
import com.bank.banking_platform.auth.exception.InvalidRefreshTokenException;
import com.bank.banking_platform.auth.repository.UserRepository;
import com.bank.banking_platform.auth.mapper.UserMapper;
import com.bank.banking_platform.auth.security.JwtService;
import com.bank.banking_platform.auth.service.AuthService;
import com.bank.banking_platform.auth.service.RefreshTokenService;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * AuthService unit testleri. Bağımlılıklar Mockito ile mocked (mock) edilmiştir.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, userMapper, passwordEncoder, jwtService,
            refreshTokenService);
    }


    private User activeUser() {
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
            .createdAt(Instant.now())
            .build();
    }

    @Test
    void shouldRegisterNewCustomerWithDefaultRole() {
        RegisterRequest request = RegisterRequest.builder()
            .email("User@Example.COM")
            .username("newuser")
            .password("Password123")
            .firstName("New")
            .lastName("User")
            .build();

        User user = activeUser();
        User savedUser = user.toBuilder().id(UUID.randomUUID()).build();
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashed-password");
        when(userMapper.toEntity(any(), anyString())).thenReturn(savedUser);
        when(userRepository.save(any(com.bank.banking_platform.auth.entity.User.class))).thenReturn(savedUser);
        UserResponse expected = UserResponse.builder()
            .id(savedUser.getId()).email("user@example.com").role(Role.CUSTOMER).build();
        when(userMapper.toResponse(savedUser)).thenReturn(expected);

        UserResponse response = authService.register(request);

        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(response.getEmail()).isEqualTo("user@example.com");
        verify(userRepository).save(argThat(u ->
            u.getRole() == Role.CUSTOMER && u.getEmail().equals("user@example.com")));
    }

    @Test
    void shouldNormalizeEmailLocaleIndependentlyUnderTurkishLocale() {
        Locale original = Locale.getDefault();
        try {
            // Türkçe (tr-TR) varsayılan locale simulation
            Locale.setDefault(new Locale("tr", "TR"));

            RegisterRequest request = RegisterRequest.builder()
                .email("ALI@EXAMPLE.COM")
                .username("aliuser")
                .password("Password123")
                .firstName("Ali")
                .lastName("Veli")
                .build();

            // Eski kod (toLowerCase() yoksa Locale.ROOT) Türkçe locale'da 'I'->'ı' dönüşümü yapar ve
            // "alı@example.com" üretir, bu da eşleşmez. Locale.ROOT ile "ali@example.com" beklenir.
            when(userRepository.existsByEmail("ali@example.com")).thenReturn(false);
            when(userRepository.existsByUsername("aliuser")).thenReturn(false);
            when(passwordEncoder.encode("Password123")).thenReturn("hashed-password");
            User savedUser = User.builder()
                .id(UUID.randomUUID()).email("ali@example.com").username("aliuser")
                .passwordHash("hashed-password").firstName("Ali").lastName("Veli")
                .role(Role.CUSTOMER).enabled(true).accountNonLocked(true).build();
            when(userMapper.toEntity(any(), anyString())).thenReturn(savedUser);
            when(userRepository.save(any(com.bank.banking_platform.auth.entity.User.class))).thenReturn(savedUser);
            UserResponse expected = UserResponse.builder()
                .id(savedUser.getId()).email("ali@example.com").role(Role.CUSTOMER).build();
            when(userMapper.toResponse(savedUser)).thenReturn(expected);

            authService.register(request);

            verify(userRepository).save(argThat(u -> u.getEmail().equals("ali@example.com")));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void shouldRejectDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
            .email("user@example.com")
            .username("other")
            .password("Password123")
            .firstName("A")
            .lastName("B")
            .build();
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void shouldRejectDuplicateUsername() {
        RegisterRequest request = RegisterRequest.builder()
            .email("other@example.com")
            .username("testuser")
            .password("Password123")
            .firstName("A")
            .lastName("B")
            .build();
        when(userRepository.existsByEmail("other@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void shouldLoginSuccessfullyAndIssueTokens() {
        User user = activeUser();
        LoginRequest request = LoginRequest.builder().identifier("user@example.com").password("Password123").build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        RefreshTokenService.RefreshTokenPair pair =
            new RefreshTokenService.RefreshTokenPair(null, "refresh-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(pair);
        when(jwtService.getAccessExpirationSeconds()).thenReturn(900L);
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder()
            .id(user.getId()).username("testuser").role(Role.CUSTOMER).build());

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
        assertThat(response.getUser().getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void shouldRejectLoginWithWrongPassword() {
        User user = activeUser();
        LoginRequest request = LoginRequest.builder().identifier("user@example.com").password("wrong").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldRejectLoginWithSameGenericMessageForUnknownUser() {
        LoginRequest request = LoginRequest.builder().identifier("unknown@example.com").password("Password123").build();
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldRejectLoginForDisabledUser() {
        User user = activeUser().toBuilder().enabled(false).build();
        LoginRequest request = LoginRequest.builder().identifier("user@example.com").password("Password123").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldRejectLoginForLockedUser() {
        User user = activeUser().toBuilder().accountNonLocked(false).build();
        LoginRequest request = LoginRequest.builder().identifier("user@example.com").password("Password123").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldRefreshTokensWithRotation() {
        User user = activeUser();
        RefreshToken refreshToken = RefreshToken.builder()
            .id(UUID.randomUUID()).user(user).tokenHash("hash").expiryDate(Instant.now().plusSeconds(3600)).revoked(false).build();
        RefreshTokenService.RefreshTokenPair pair =
            new RefreshTokenService.RefreshTokenPair(refreshToken, "new-refresh-token");
        when(refreshTokenService.rotateRefreshToken("old-refresh-token")).thenReturn(pair);
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(jwtService.getAccessExpirationSeconds()).thenReturn(900L);
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder()
            .id(user.getId()).username("testuser").role(Role.CUSTOMER).build());

        AuthResponse response = authService.refresh("old-refresh-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        when(refreshTokenService.rotateRefreshToken("expired-token"))
            .thenThrow(new InvalidRefreshTokenException("süresi dolmuş"));

        assertThatThrownBy(() -> authService.refresh("expired-token"))
            .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void shouldRevokeTokenOnLogout() {
        User user = activeUser();
        authService.logout("refresh-token", user);
        verify(refreshTokenService).revokeToken("refresh-token", user);
    }

    @Test
    void shouldReturnCurrentUserWithoutPassword() {
        User user = activeUser();
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder()
            .id(user.getId()).username("testuser").role(Role.CUSTOMER).build());

        UserResponse response = authService.getCurrentUser(user);

        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
    }
}

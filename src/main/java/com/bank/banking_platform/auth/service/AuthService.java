package com.bank.banking_platform.auth.service;

import com.bank.banking_platform.auth.dto.AuthResponse;
import com.bank.banking_platform.auth.dto.LoginRequest;
import com.bank.banking_platform.auth.dto.RegisterRequest;
import com.bank.banking_platform.auth.dto.UserResponse;
import com.bank.banking_platform.auth.entity.Role;
import com.bank.banking_platform.auth.entity.User;
import com.bank.banking_platform.auth.exception.AuthenticationException;
import com.bank.banking_platform.auth.exception.DuplicateResourceException;
import com.bank.banking_platform.auth.repository.UserRepository;
import com.bank.banking_platform.auth.mapper.UserMapper;
import com.bank.banking_platform.auth.security.JwtService;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kimlik doğrulama iş mantığını yönetir.
 * Controller'dan gelen istekleri işler ve veritabanı/token servisleriyle koordinasyon sağlar.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Yeni kullanıcı kaydı oluşturur.
     * Varsayılan rol CUSTOMER'dır; genel kayıt üzerinden ADMIN/SUPPORT atanamaz.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Bu e-posta adresi zaten kayıtlı.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Bu kullanıcı adı zaten kullanılıyor.");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        User user = userMapper.toEntity(request, passwordHash);
        user.setEmail(email);
        // Genel kayıt endpoint'i üzerinden asla ADMIN/SUPPORT rolü atanmaz.
        user.setRole(Role.CUSTOMER);

        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    /**
     * E-posta veya kullanıcı adı ile giriş yapar.
     * Kullanıcının varlığı/olmadığı hakkında bilgi vermemek için tüm hatalar genel bir mesajla döner.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim();
        String emailCandidate = identifier.toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(emailCandidate)
            .orElseGet(() -> userRepository.findByUsername(identifier).orElse(null));

        if (user == null) {
            // Varlığın ortaya çıkmasını engellemek için zaman açısından güvenli bir işlem yapılır
            // (şifreleme çalıştırılır ancak sonuç atılır). Tüm hatalar aynı genel mesajla döner.
            passwordEncoder.encode("dummy");
            throw new AuthenticationException("E-posta/kullanıcı adı veya parola hatalı.");
        }

        if (!user.isEnabled() || !user.isAccountNonLocked()) {
            throw new AuthenticationException("E-posta/kullanıcı adı veya parola hatalı.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("E-posta/kullanıcı adı veya parola hatalı.");
        }

        return issueTokens(user);
    }

    /**
     * Refresh Token ile Access Token'ı yeniler (rotation ile).
     */
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshTokenService.RefreshTokenPair pair = refreshTokenService.rotateRefreshToken(refreshToken);
        User user = pair.refreshToken().getUser();

        String accessToken = jwtService.generateAccessToken(user);
        long expiresIn = jwtService.getAccessExpirationSeconds();

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(pair.plainToken())
            .tokenType("Bearer")
            .expiresIn(expiresIn)
            .expiresAt(Instant.now().plusSeconds(expiresIn))
            .user(userMapper.toResponse(user))
            .build();
    }

    /**
     * İlgili Refresh Token'ı iptal eder (çıkış).
     */
    @Transactional
    public void logout(String refreshToken, User currentUser) {
        refreshTokenService.revokeToken(refreshToken, currentUser);
    }

    /**
     * Giriş yapmış kullanıcının bilgilerini döner (hassas alanlar hariç).
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(User user) {
        return userMapper.toResponse(user);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        RefreshTokenService.RefreshTokenPair pair = refreshTokenService.createRefreshToken(user);
        long expiresIn = jwtService.getAccessExpirationSeconds();

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(pair.plainToken())
            .tokenType("Bearer")
            .expiresIn(expiresIn)
            .expiresAt(Instant.now().plusSeconds(expiresIn))
            .user(userMapper.toResponse(user))
            .build();
    }

    private String normalizeEmail(String email) {
        // Locale.ROOT ile yerel-ayara bağımsız (ör. Türkçe'de 'I'->'ı' problemi olmaz).
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}

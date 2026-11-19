package com.bank.banking_platform.auth.initializer;

import com.bank.banking_platform.auth.entity.Role;
import com.bank.banking_platform.auth.entity.User;
import com.bank.banking_platform.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

/**
 * Development ortamında tek seferlik admin hesabı başlatıcısı.
 * <p>
 * Sadece "dev" profili aktifken çalışır. Production'da hiçbir şekilde çalışmaz.
 * Environment variables'dan okur, mevcut bir admin varsa oluşturmaz.
 * </p>
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevAdminInitializer {

    private final Environment environment;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Environment variable isimleri
    private static final String ADMIN_USERNAME_ENV = "BANKING_ADMIN_USERNAME";
    private static final String ADMIN_EMAIL_ENV = "BANKING_ADMIN_EMAIL";
    private static final String ADMIN_PASSWORD_ENV = "BANKING_ADMIN_PASSWORD";

    // Özel değerler (sadece initializer içeriği, dışarıça output'a yazılmaz)
    private static final String DEFAULT_FIRST_NAME = "Admin";
    private static final String DEFAULT_LAST_NAME = "User";

    /**
     * Development profilde uygulama başlatıldığında çalışan initializer.
     * Environment variable'lar kontrol edilir ve admin kullanıcısı oluşturulur.
     * <p>
     * Kurallar:
     * - Tüm three (username, email, password) environment variable present ve non-empty olmalı
     * - Zaten mevcut bir admin varsa tekrar oluşturulmaz
     * - Var olan CUSTOMER hesabının rolü ADMIN'e otomatik olarak yükseltilmez
     * - Plain-text şifre herhangi bir yerde saklanmaz veya loglanmaz
     * - Unique email/username kısıtları respected (varsa oluşturulmaz)
     * - Production profiline hiçbir şekilde girmez
     */
    @PostConstruct
    public void initialize() {
        // Environment variable'ları oku
        String adminUsername = environment.getProperty(ADMIN_USERNAME_ENV);
        String adminEmail = environment.getProperty(ADMIN_EMAIL_ENV);
        String adminPassword = environment.getProperty(ADMIN_PASSWORD_ENV);

        // Tüm three env var present ve non-empty olmalı
        if (adminUsername == null || adminUsername.isBlank() ||
            adminEmail == null || adminEmail.isBlank() ||
            adminPassword == null || adminPassword.isBlank()) {
            // Eksik variable varsa admin oluşturma, app hala başlar
            // Varsayılan şifre kullanmaz (güvenlik ilknesi)
            return;
        }

        // Zaten admin var mı? (email veya username ile - unique constraint'ler)
        Optional<User> existingByEmail = userRepository.findByEmail(adminEmail);
        Optional<User> existingByUsername = userRepository.findByUsername(adminUsername);

        if (existingByEmail.isPresent() || existingByUsername.isPresent()) {
            // Admin zaten varsa tekrar oluşturma, rollerini yükseltme
            // CUSTOMER hesabının rolü ADMIN'e otomatik olarak yükseltilmez (kural)
            return;
        }

        // Yeni admin kullanıcı oluştur
        User adminUser = User.builder()
                .email(adminEmail)
                .username(adminUsername)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .role(Role.ADMIN)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        userRepository.save(adminUser);
    }
}
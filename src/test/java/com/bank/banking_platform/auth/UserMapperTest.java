package com.bank.banking_platform.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.bank.banking_platform.auth.dto.RegisterRequest;
import com.bank.banking_platform.auth.dto.UserResponse;
import com.bank.banking_platform.auth.entity.Role;
import com.bank.banking_platform.auth.entity.User;
import com.bank.banking_platform.auth.mapper.UserMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * UserMapper unit testleri. Entity nesnesi doğrudan dışarı açılmamalı, DTO üzerinden yanıt
 * dönmeli ve şifre hash'i yanıtta olmamalıdır.
 */
class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void shouldMapEntityToResponseWithoutPassword() {
        User user = User.builder()
            .id(java.util.UUID.randomUUID())
            .email("user@example.com")
            .username("testuser")
            .passwordHash("hashed-password-must-not-leak")
            .firstName("Test")
            .lastName("User")
            .role(Role.CUSTOMER)
            .enabled(true)
            .accountNonLocked(true)
            .createdAt(Instant.parse("2026-01-01T10:00:00Z"))
            .build();

        UserResponse response = mapper.toResponse(user);

        assertThat(response.getEmail()).isEqualTo("user@example.com");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getFullName()).isEqualTo("Test User");
        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(response.isEnabled()).isTrue();
        assertThat(response.isAccountNonLocked()).isTrue();
        // Şifre hash'i yanıtta olmamalıdır (UserResponse'da hiçbir şifre alanı yoktur).
    }

    @Test
    void shouldMapRegisterRequestToEntityWithCustomerRoleAndNormalizedEmail() {
        RegisterRequest request = RegisterRequest.builder()
            .email("ADMIN@Example.COM")
            .username("newuser")
            .password("Password123")
            .firstName("New")
            .lastName("User")
            .build();

        User entity = mapper.toEntity(request, "hashed-password");

        assertThat(entity.getEmail()).isEqualTo("admin@example.com");
        assertThat(entity.getUsername()).isEqualTo("newuser");
        assertThat(entity.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(entity.isEnabled()).isTrue();
        assertThat(entity.isAccountNonLocked()).isTrue();
        assertThat(entity.getPasswordHash()).isEqualTo("hashed-password");
    }
}

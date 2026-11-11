package com.bank.banking_platform.auth.mapper;

import com.bank.banking_platform.auth.dto.RegisterRequest;
import com.bank.banking_platform.auth.dto.UserResponse;
import com.bank.banking_platform.auth.entity.User;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * User entity ve DTO arasında dönüşüm yapar.
 * Entity nesneleri doğrudan dışarı açılmaz; her zaman DTO üzerinden yanıt döner.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .username(user.getUsername())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .fullName(user.getFullName())
            .role(user.getRole())
            .enabled(user.isEnabled())
            .accountNonLocked(user.isAccountNonLocked())
            .createdAt(user.getCreatedAt())
            .build();
    }

    public User toEntity(RegisterRequest request, String passwordHash) {
        return User.builder()
            .email(normalizeEmail(request.getEmail()))
            .username(request.getUsername())
            .passwordHash(passwordHash)
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .role(com.bank.banking_platform.auth.entity.Role.CUSTOMER)
            .enabled(true)
            .accountNonLocked(true)
            .build();
    }

    private String normalizeEmail(String email) {
        // Locale.ROOT ile yerel-ayara bağımsız (ör. Türkçe'de 'I'->'ı' problemi olmaz).
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}

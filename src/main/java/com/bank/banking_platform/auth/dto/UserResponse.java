package com.bank.banking_platform.auth.dto;

import com.bank.banking_platform.auth.entity.Role;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kullanıcı yanıt DTO'su.
 * Şifre hash'i gibi gizli alanlar içermez.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private java.util.UUID id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private Role role;
    private boolean enabled;
    private boolean accountNonLocked;
    private Instant createdAt;
}

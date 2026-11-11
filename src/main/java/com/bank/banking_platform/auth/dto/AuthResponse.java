package com.bank.banking_platform.auth.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kimlik doğrulama yanıtı (giriş / token yenileme).
 * Şifre gibi hassas alan içermez.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private Instant expiresAt;
    private UserResponse user;
}

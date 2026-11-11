package com.bank.banking_platform.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Giriş isteği. E-posta veya kullanıcı adı ile giriş yapılabilir.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "E-posta veya kullanıcı adı zorunludur.")
    @Size(max = 255)
    private String identifier;

    @NotBlank(message = "Parola zorunludur.")
    private String password;
}

package com.bank.banking_platform.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kullanıcı kayıt isteği.
 * Roller (ADMIN / SUPPORT) burada kabul edilmez; varsayılan olarak CUSTOMER atanır.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "E-posta alanı zorunludur.")
    @Email(message = "Geçerli bir e-posta adresi giriniz.")
    private String email;

    @NotBlank(message = "Kullanıcı adı alanı zorunludur.")
    @Size(min = 3, max = 30, message = "Kullanıcı adı 3 ile 30 karakter arasında olmalıdır.")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$",
        message = "Kullanıcı adı sadece harf, rakam, alt çizgi, nokta ve tire içerebilir.")
    private String username;

    @NotBlank(message = "Parola alanı zorunludur.")
    @Size(min = 8, message = "Parola en az 8 karakter olmalıdır.")
    private String password;

    @NotBlank(message = "İsim alanı zorunludur.")
    @Size(max = 100, message = "İsim en fazla 100 karakter olabilir.")
    private String firstName;

    @NotBlank(message = "Soyisim alanı zorunludur.")
    @Size(max = 100, message = "Soyisim en fazla 100 karakter olabilir.")
    private String lastName;
}

package com.bank.banking_platform.auth.exception;

/**
 * Kimlik doğrulama başarısızlığı olduğuunda fırlatılır.
 * Kullanıcının varlığı/olmadığı hakkında bilgi vermemek için genel bir mesaj kullanılır.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

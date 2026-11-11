package com.bank.banking_platform.auth.exception;

/**
 * Refresh token geçersiz, süresi dolmuş, iptal edilmiş veya tekrar kullanıldığında fırlatılır.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }

    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}

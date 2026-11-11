package com.bank.banking_platform.auth.exception;

/**
 * Kaynak bulunamadığıunda fırlatılır (ör. kullanıcı bulunamadı).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

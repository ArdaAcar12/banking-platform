package com.bank.banking_platform.auth.exception;

/**
 * Kaynak zaten mevcut olduğunda fırlatılır (ör. tekrarlanan e-posta veya kullanıcı adı).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}

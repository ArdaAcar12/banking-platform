package com.bank.banking_platform.auth.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tutarlı hata yanıtı modeli.
 * Stack trace, SQL hata detayı veya şifre bilgisi içermez.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}

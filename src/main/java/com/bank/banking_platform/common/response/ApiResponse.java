package com.bank.banking_platform.common.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Genel başarı yanıtı sarmalayıcısı.
 * Tüm başarıyan API yanıtları bu yapı ile döndürülebilir.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private LocalDateTime timestamp;
    private boolean success;
    private T data;
    private String message;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .timestamp(LocalDateTime.now())
            .success(true)
            .data(data)
            .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .timestamp(LocalDateTime.now())
            .success(true)
            .data(data)
            .message(message)
            .build();
    }
}

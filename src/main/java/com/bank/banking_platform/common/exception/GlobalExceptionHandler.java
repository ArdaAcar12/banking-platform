package com.bank.banking_platform.common.exception;

import com.bank.banking_platform.auth.dto.ApiError;
import com.bank.banking_platform.auth.exception.AuthenticationException;
import com.bank.banking_platform.auth.exception.DuplicateResourceException;
import com.bank.banking_platform.auth.exception.InvalidRefreshTokenException;
import com.bank.banking_platform.auth.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Merkezi hata yönetimi.
 * Tüm hata yanıtları tutarlı bir {@link ApiError} JSON formatında döner.
 * Stack trace, SQL hatası veya şifre bilgisi dışarıya sızdırılmaz.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, "Doğrulama hatası: " + message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        String message = ex.getConstraintViolations().stream()
            .map(error -> error.getPropertyPath() + ": " + error.getMessage())
            .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, "Doğrulama hatası: " + message, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiError> handleSecurityAuthentication(
        org.springframework.security.core.AuthenticationException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Kimlik doğrulama başarısız.", request);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiError> handleInvalidRefreshToken(InvalidRefreshTokenException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiError> handleJwt(JwtException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Geçersiz erişim token'ı.", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, "Bu işlem için yetkiniz bulunmamaktadır.", request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, "E-posta veya kullanıcı adı zaten kayıtlı.", request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandler(NoHandlerFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, "Endpoint bulunamadı.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, WebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmeyen bir sunucu hatası oluştu.", request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, WebRequest request) {
        String path = request != null ? request.getDescription(false) : "";
        if (path.startsWith("uri=")) {
            path = path.substring(4);
        }
        ApiError error = ApiError.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(path)
            .build();
        return ResponseEntity.status(status).body(error);
    }
}

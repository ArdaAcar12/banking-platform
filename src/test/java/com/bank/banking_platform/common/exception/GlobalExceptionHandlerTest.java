package com.bank.banking_platform.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.bank.banking_platform.auth.dto.ApiError;
import com.bank.banking_platform.auth.exception.AuthenticationException;
import com.bank.banking_platform.auth.exception.DuplicateResourceException;
import com.bank.banking_platform.auth.exception.InvalidRefreshTokenException;
import com.bank.banking_platform.auth.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * GlobalExceptionHandler unit testleri. Tüm hata yanıtları tutarlı JSON formatında ve
 * uygun HTTP status koduyla dönülmelidir.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        HttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        request = new ServletWebRequest(servletRequest);
    }

    @Test
    void shouldReturn400ForValidationErrors() {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "object");
        result.addError(new FieldError("object", "password", "parola en az 8 karakter olmalıdır"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, result);

        ResponseEntity<ApiError> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("password");
        assertThat(response.getBody().getPath()).contains("/api/v1/auth/login");
    }

    @Test
    void shouldReturn401ForAuthenticationFailure() {
        AuthenticationException ex = new AuthenticationException("E-posta/kullanıcı adı veya parola hatalı.");

        ResponseEntity<ApiError> response = handler.handleAuthentication(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo("E-posta/kullanıcı adı veya parola hatalı.");
    }

    @Test
    void shouldReturn401ForInvalidRefreshToken() {
        InvalidRefreshTokenException ex = new InvalidRefreshTokenException("Geçersiz refresh token.");

        ResponseEntity<ApiError> response = handler.handleInvalidRefreshToken(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo("Geçersiz refresh token.");
    }

    @Test
    void shouldReturn401ForJwtException() {
        JwtException ex = new JwtException("Token expired");

        ResponseEntity<ApiError> response = handler.handleJwt(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn403ForAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Yetkisiz");

        ResponseEntity<ApiError> response = handler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }

    @Test
    void shouldReturn404ForNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Kullanıcı bulunamadı.");

        ResponseEntity<ApiError> response = handler.handleNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("Kullanıcı bulunamadı.");
    }

    @Test
    void shouldReturn409ForDuplicateResource() {
        DuplicateResourceException ex = new DuplicateResourceException("Bu e-posta adresi zaten kayıtlı.");

        ResponseEntity<ApiError> response = handler.handleDuplicate(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    void shouldReturn500ForUnexpectedError() {
        RuntimeException ex = new RuntimeException("unexpected");

        ResponseEntity<ApiError> response = handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
    }

    @Test
    void shouldNotExposeStackTraceOrSensitiveDetails() {
        RuntimeException ex = new RuntimeException("db error: select * from users where...");
        ResponseEntity<ApiError> response = handler.handleGeneric(ex, request);

        assertThat(response.getBody().getMessage()).isEqualTo("Beklenmeyen bir sunucu hatası oluştu.");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(List.of(response.getBody().getMessage(),
            response.getBody().getError())).noneMatch(s -> s.contains("stack") || s.contains("select"));
    }
}

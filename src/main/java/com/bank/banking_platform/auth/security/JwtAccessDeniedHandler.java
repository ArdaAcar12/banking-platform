package com.bank.banking_platform.auth.security;

import com.bank.banking_platform.auth.dto.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Kimliği doğrulanmış ancak yetkisi olmayan kullanıcıya 403 döner.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError error = ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpServletResponse.SC_FORBIDDEN)
            .error("Forbidden")
            .message("Bu işlem için yetkiniz bulunmamaktadır.")
            .path(request.getRequestURI())
            .build();
        objectMapper.writeValue(response.getWriter(), error);
    }
}

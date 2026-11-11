package com.bank.banking_platform.auth.controller;

import com.bank.banking_platform.auth.dto.ApiError;
import com.bank.banking_platform.auth.dto.UserResponse;
import com.bank.banking_platform.auth.entity.User;
import com.bank.banking_platform.auth.exception.ResourceNotFoundException;
import com.bank.banking_platform.auth.repository.UserRepository;
import com.bank.banking_platform.auth.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin endpoint'leri.
 * Bu endpoint'ler yalnızca ADMIN rolüne açıktır.
 */
@io.swagger.v3.oas.annotations.tags.Tag(name = "Admin", description = "Yönetici işlemleri (yalnızca ADMIN)")
@RequestMapping(path = "/api/v1/admin", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AdminController(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Operation(
        summary = "Kullanıcı listesini getir",
        description = "Tüm kullanıcıların listesini döner. Şifre hash'i gibi gizli alanlar içermez.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Kullanıcı listesi",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "403", description = "Yetkisiz erişim (ADMIN gerekli)",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulaması gerekli",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
            .map(userMapper::toResponse)
            .toList();
        return ResponseEntity.ok(users);
    }

    @Operation(
        summary = "Kullanıcı detayını getir",
        description = "Belirtilen ID'ye sahip kullanıcının bilgilerini döner.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Kullanıcı bilgileri",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Yetkisiz erişim (ADMIN gerekli)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(
        @Parameter(description = "Kullanıcı UUID'si") @PathVariable UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı."));
        return ResponseEntity.ok(userMapper.toResponse(user));
    }
}

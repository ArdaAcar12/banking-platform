package com.bank.banking_platform.account.controller;

import com.bank.banking_platform.account.dto.AccountListResponse;
import com.bank.banking_platform.account.dto.AccountRequest;
import com.bank.banking_platform.account.dto.AccountResponse;
import com.bank.banking_platform.account.service.AccountService;
import com.bank.banking_platform.auth.exception.AuthenticationException;
import com.bank.banking_platform.auth.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hesap yönetimi endpoint'leri.
 * Kullanıcı yalnızca kendi hesaplarını görüntüleyebilir ve yönetebilir.
 */
@RestController
@RequestMapping(path = "/api/v1/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Accounts", description = "Hesap yönetimi işlemleri (listeleme, detay, hesap açma)")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(
        summary = "Hesapları listele",
        description = "Giriş yapmış kullanıcının kendi hesaplarını döndürür."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Hesap listesi",
            content = @Content(schema = @Schema(implementation = AccountListResponse.class))),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulaması gerekli",
            content = @Content(schema = @Schema(implementation = com.bank.banking_platform.auth.dto.ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<AccountListResponse> listAccounts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID currentUserId = resolveCurrentUserId(userDetails);
        return ResponseEntity.ok(accountService.listAccounts(currentUserId));
    }

    @Operation(
        summary = "Hesap detayı",
        description = "Belirtilen ID'ye sahip hesabı döndürür. Yalnızca hesap sahibi erişebilir."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Hesap detayı",
            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulaması gerekli",
            content = @Content(schema = @Schema(implementation = com.bank.banking_platform.auth.dto.ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Yetkisiz erişim",
            content = @Content(schema = @Schema(implementation = com.bank.banking_platform.auth.dto.ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Hesap bulunamadı",
            content = @Content(schema = @Schema(implementation = com.bank.banking_platform.auth.dto.ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID currentUserId = resolveCurrentUserId(userDetails);
        return ResponseEntity.ok(accountService.getAccount(currentUserId, id));
    }

    @Operation(
        summary = "Yeni hesap aç",
        description = "Giriş yapmış kullanıcı için yeni bir hesap açar. "
            + "Yalnızca hesap türü ve para birimi kabul edilir."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Hesap açıldı",
            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek / doğrulama hatası",
            content = @Content(schema = @Schema(implementation = com.bank.banking_platform.auth.dto.ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulaması gerekli",
            content = @Content(schema = @Schema(implementation = com.bank.banking_platform.auth.dto.ApiError.class))),
        @ApiResponse(responseCode = "409", description = "Aynı tür ve para biriminde aktif hesap zaten mevcut",
            content = @Content(schema = @Schema(implementation = com.bank.banking_platform.auth.dto.ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<AccountResponse> openAccount(
            @Valid @RequestBody AccountRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID currentUserId = resolveCurrentUserId(userDetails);
        AccountResponse response = accountService.openAccount(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private UUID resolveCurrentUserId(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AuthenticationException("Oturum bulunamadı.");
        }
        return userDetails.getId();
    }
}

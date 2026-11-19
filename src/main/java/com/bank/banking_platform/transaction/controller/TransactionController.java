package com.bank.banking_platform.transaction.controller;

import com.bank.banking_platform.account.repository.AccountRepository;
import com.bank.banking_platform.transaction.dto.TransactionListResponse;
import com.bank.banking_platform.transaction.dto.TransactionResponse;
import com.bank.banking_platform.transaction.entity.TransactionStatus;
import com.bank.banking_platform.transaction.entity.TransactionType;
import com.bank.banking_platform.transaction.service.TransactionService;
import com.bank.banking_platform.auth.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Transactions", description = "İşlem geçmişi işlemleri (listeleme, detay, filtreleme)")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountRepository accountRepository;

    public TransactionController(
        TransactionService transactionService,
        AccountRepository accountRepository
    ) {
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
    }

    @Operation(
        summary = "İşlem geçmişini listele",
        description = "Giriş yapmış kullanıcının kendi hesaplarına ait işlemleri sayfalı olarak döndürür."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "İşlem listesi",
            content = @Content(schema = @Schema(implementation = TransactionListResponse.class))),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek parametreleri",
            content = @Content(schema = @Schema(implementation = com.bank.banking_platform.auth.dto.ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulaması gerekli",
            content = @Content(schema = @Schema(implementation = com.bank.banking_platform.auth.dto.ApiError.class)))
    })
    @GetMapping("/api/v1/transactions")
    public ResponseEntity<TransactionListResponse> listTransactions(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) TransactionType type,
        @RequestParam(required = false) TransactionStatus status,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @PageableDefault(page = 0, size = 20, sort = "createdAt,desc") Pageable pageable
    ) {
        UUID currentUserId = resolveCurrentUserId(userDetails);
        TransactionListResponse response = transactionService.listTransactions(
            currentUserId,
            pageable.getPageNumber(),
            pageable.getPageSize(),
            pageable.getSort().toString(),
            type,
            status,
            startDate,
            endDate
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/accounts/{accountId}/transactions")
    public ResponseEntity<TransactionListResponse> listTransactionsByAccount(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable UUID accountId
    ) {
        UUID currentUserId = resolveCurrentUserId(userDetails);
        TransactionListResponse response = transactionService.listTransactionsByAccount(
            accountId, currentUserId, 0, 10, "createdAt,desc", null, null, null, null
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "İşlem detayı",
        description = "Belirtilen ID'ye sahip işlem detayını döndürür. Yalnızca işlem sahibi erişebilir."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "İşlem detayı",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulaması gerekli",
            content = @Content(schema = @Schema(implementation = com.bank.banking_platform.auth.dto.ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Yetkisiz erişim",
            content = @Content(schema = @Schema(implementation = com.bank.banking_platform.auth.dto.ApiError.class))),
        @ApiResponse(responseCode = "404", description = "İşlem bulunamadı",
            content = @Content(schema = @Schema(implementation = com.bank.banking_platform.auth.dto.ApiError.class)))
    })
    @GetMapping("/api/v1/transactions/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable UUID id
    ) {
        UUID currentUserId = resolveCurrentUserId(userDetails);
        TransactionResponse response = transactionService.getTransaction(currentUserId, id);
        return ResponseEntity.ok(response);
    }

    private UUID resolveCurrentUserId(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new com.bank.banking_platform.auth.exception.AuthenticationException(
                "Oturum bulunamadı.");
        }
        return userDetails.getId();
    }
}
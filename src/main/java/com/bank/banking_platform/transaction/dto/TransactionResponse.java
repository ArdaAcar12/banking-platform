package com.bank.banking_platform.transaction.dto;

import com.bank.banking_platform.transaction.entity.TransactionStatus;
import com.bank.banking_platform.transaction.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Finansal işlem yanıt DTO'su.
 * Kullanıcıya güvenli şekilde işlem bilgilerini döndürür.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private UUID id;
    private String referenceNumber;
    private TransactionType transactionType;
    private TransactionStatus status;
    private BigDecimal amount;
    private String currency;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}

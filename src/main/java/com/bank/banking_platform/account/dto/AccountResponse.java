package com.bank.banking_platform.account.dto;

import com.bank.banking_platform.account.entity.AccountStatus;
import com.bank.banking_platform.account.entity.AccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Hesap yanıt DTO'su.
 * Kullanıcıya güvenli şekilde hesap bilgilerini döndürür.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private UUID id;
    private String accountNumber;
    private String iban;
    private AccountType accountType;
    private String currency;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

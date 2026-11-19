package com.bank.banking_platform.transaction.entity;

import com.bank.banking_platform.account.entity.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Finansal işlem ledger kaydı (çift taraflı muhasebe).
 * <p>
 * Güvenlik kuralları:
 * <ul>
 *   <li>Primary key UUID'dir.</li>
 *   <li>Her kayıt bir FinancialTransaction ve bir Account'a bağlıdır.</li>
 *   <li>Tutar pozitif olmalı ve {@link BigDecimal} ile saklanır.</li>
 *   <li>Kayıt oluşturulduktan sonra değiştirilmemeli veya silinmemeli (immutable).</li>
 *   <li>Çift taraflı işlemde bir DEBIT ve bir CREDIT kaydı aynı transaction ID'ye bağlanır.</li>
 * </ul>
 */
@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
    @jakarta.persistence.JoinColumn(name = "transaction_id", nullable = false)
    private FinancialTransaction transaction;

    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
    @jakarta.persistence.JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private LedgerDirection direction;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

package com.bank.banking_platform.account.entity;

import com.bank.banking_platform.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Banka hesap entity'si.
 * <p>
 * Güvenlik kuralları:
 * <ul>
 *   <li>Primary key UUID'dir.</li>
 *   <li>Hesap numarası ve IBAN benzersizdir.</li>
 *   <li>Bakiye ve kullanılabilir bakiye {@link BigDecimal} ile saklanır.</li>
 *   <li>Hesap sahibi kullanıcıyla foreign key ile ilişkilendirilir.</li>
 *   <li>Hesap sahibi değişikliği yapılmaz (user immutable).</li>
 * </ul>
 */
@Entity
@Table(
    name = "accounts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_accounts_account_number", columnNames = "account_number"),
        @UniqueConstraint(name = "uk_accounts_iban", columnNames = "iban")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "account_number", nullable = false, length = 34, unique = true)
    private String accountNumber;

    @Column(name = "iban", nullable = false, length = 34, unique = true)
    private String iban;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "available_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

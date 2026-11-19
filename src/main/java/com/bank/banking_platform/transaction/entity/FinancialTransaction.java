package com.bank.banking_platform.transaction.entity;

import com.bank.banking_platform.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
import org.hibernate.annotations.CreationTimestamp;

/**
 * Finansal işlem ana kaydı.
 * <p>
 * Güvenlik kuralları:
 * <ul>
 *   <li>Primary key UUID'dir.</li>
 *   <li>Referans numarası benzersizdir.</li>
 *   <li>Tutar pozitif olmalı ve {@link BigDecimal} ile saklanır.</li>
 *   <li>İşlem kayıtları oluşturulduktan sonra değiştirilmemeli (immutable).</li>
 *   <li>createdAt backend tarafından belirlenir, completedAt yalnızca işlem tamamlandığında doldurulur.</li>
 * </ul>
 */
@Entity
@Table(
    name = "financial_transactions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_financial_transactions_reference_number", columnNames = "reference_number")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reference_number", nullable = false, length = 50, unique = true)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
    @jakarta.persistence.JoinColumn(name = "initiated_by", nullable = false)
    private User initiatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at", nullable = true)
    private LocalDateTime completedAt;
}

package com.bank.banking_platform.transaction.repository;

import com.bank.banking_platform.transaction.entity.FinancialTransaction;
import com.bank.banking_platform.transaction.entity.TransactionStatus;
import com.bank.banking_platform.transaction.entity.TransactionType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Finansal işlem veritabanı işlemleri.
 * Tüm sorgular kullanıcı bazlıdır; başka bir kullanıcının işlemlerine
 * erişim engellenir.
 */
@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {

    Page<FinancialTransaction> findByInitiatedById(@Param("userId") UUID userId, Pageable pageable);

    Page<FinancialTransaction> findByInitiatedByIdAndTransactionType(
        @Param("userId") UUID userId,
        @Param("transactionType") TransactionType transactionType,
        Pageable pageable
    );

    Page<FinancialTransaction> findByInitiatedByIdAndStatus(
        @Param("userId") UUID userId,
        @Param("status") TransactionStatus status,
        Pageable pageable
    );

    Page<FinancialTransaction> findByInitiatedByIdAndCreatedAtBetween(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    Page<FinancialTransaction> findByInitiatedByIdAndTransactionTypeAndStatusAndCreatedAtBetween(
        @Param("userId") UUID userId,
        @Param("transactionType") TransactionType transactionType,
        @Param("status") TransactionStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    @Query("SELECT f FROM FinancialTransaction f WHERE f.id = :id AND f.initiatedBy.id = :userId")
    FinancialTransaction findByIdAndInitiatedById(@Param("id") UUID id, @Param("userId") UUID userId);

    Page<FinancialTransaction> findByIdInAndInitiatedById(
        @Param("ids") List<UUID> ids,
        @Param("userId") UUID userId,
        Pageable pageable
    );
}

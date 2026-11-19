package com.bank.banking_platform.transaction.repository;

import com.bank.banking_platform.transaction.entity.LedgerEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Ledger kayıt veritabanı işlemleri.
 * Tüm sorgular hesap ve işlem bazlıdır.
 */
@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByTransactionId(@Param("transactionId") UUID transactionId);

    List<LedgerEntry> findByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT l FROM LedgerEntry l WHERE l.transaction.id = :transactionId AND l.account.id = :accountId")
    List<LedgerEntry> findByTransactionIdAndAccountId(
        @Param("transactionId") UUID transactionId,
        @Param("accountId") UUID accountId
    );

    @Query("SELECT DISTINCT l.transaction.id FROM LedgerEntry l WHERE l.account.id = :accountId")
    List<UUID> findTransactionIdsByAccountId(@Param("accountId") UUID accountId);
}

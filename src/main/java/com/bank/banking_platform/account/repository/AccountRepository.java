package com.bank.banking_platform.account.repository;

import com.bank.banking_platform.account.entity.Account;
import com.bank.banking_platform.account.entity.AccountType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Hesap veritabanı işlemleri.
 * Tüm sorgular kullanıcı bazlıdır; başka bir kullanıcının hesabına
 * erişim engellenir.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUserId(@Param("userId") UUID userId);

    Optional<Account> findByUserIdAndId(@Param("userId") UUID userId, @Param("id") UUID id);

    boolean existsByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    Optional<Account> findByIban(@Param("iban") String iban);

    Optional<Account> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.user.id = :userId AND a.accountType = :accountType AND a.currency = :currency AND a.status = 'ACTIVE'")
    Long countByUserIdAndAccountTypeAndCurrency(
        @Param("userId") UUID userId,
        @Param("accountType") AccountType accountType,
        @Param("currency") String currency
    );
}

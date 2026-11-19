package com.bank.banking_platform.account.service;

import com.bank.banking_platform.account.dto.AccountRequest;
import com.bank.banking_platform.account.dto.AccountResponse;
import com.bank.banking_platform.account.dto.AccountListResponse;
import com.bank.banking_platform.account.entity.Account;
import com.bank.banking_platform.account.entity.AccountStatus;
import com.bank.banking_platform.account.entity.AccountType;
import com.bank.banking_platform.account.repository.AccountRepository;
import com.bank.banking_platform.auth.entity.User;
import com.bank.banking_platform.auth.exception.DuplicateResourceException;
import com.bank.banking_platform.auth.exception.ResourceNotFoundException;
import com.bank.banking_platform.auth.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hesap iş mantığını yönetir.
 * <p>
 * Güvenlik kuralları:
 * <ul>
 *   <li>Her işlemde hesap sahibi (userId) SecurityContext üzerinden belirlenir.</li>
 *   <li>Kullanıcı yalnızca kendi hesaplarını görebilir ve yönetebilir.</li>
 *   <li>Hesap numarası ve IBAN backend tarafından üretilir.</li>
 *   <li>Başlangıç bakiyesi sıfırdır.</li>
 *   <li>Kullanıcı request üzerinden bakiye veya sahipID gönderemez.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    /**
     * Kullanıcının tüm hesaplarını döndürür.
     */
    @Transactional(readOnly = true)
    public AccountListResponse listAccounts(UUID userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        List<AccountResponse> responses = accounts.stream()
            .map(this::toResponse)
            .toList();
        return AccountListResponse.builder().data(responses).build();
    }

    /**
     * Belirtilen ID'ye sahip hesabı döndürür.
     * Yalnızca hesap sahibi erişebilir.
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID userId, UUID accountId) {
        Account account = accountRepository.findByUserIdAndId(userId, accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Hesap bulunamadı."));
        return toResponse(account);
    }

    /**
     * Kullanıcı için yeni hesap açar.
     * <p>
     * Request üzerinden yalnızca accountType ve currency kabul edilir.
     * Bakiye, IBAN, hesap numarası backend tarafından oluşturulur.
     */
    @Transactional
    public AccountResponse openAccount(UUID userId, AccountRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı."));

        // Aynı tür ve para biriminde aktif hesap varsa tekrar açılmaz.
        Long count = accountRepository.countByUserIdAndAccountTypeAndCurrency(
            userId,
            request.getAccountType(),
            request.getCurrency()
        );
        if (count > 0) {
            throw new DuplicateResourceException(
                "Aynı tür ve para biriminde aktif bir hesap zaten mevcut."
            );
        }

        String accountNumber = generateAccountNumber();
        String iban = generateIban();

        Account account = Account.builder()
            .user(user)
            .accountNumber(accountNumber)
            .iban(iban)
            .accountType(request.getAccountType())
            .currency(request.getCurrency())
            .balance(BigDecimal.ZERO)
            .availableBalance(BigDecimal.ZERO)
            .status(AccountStatus.ACTIVE)
            .build();

        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
            .id(account.getId())
            .accountNumber(account.getAccountNumber())
            .iban(account.getIban())
            .accountType(account.getAccountType())
            .currency(account.getCurrency())
            .balance(account.getBalance())
            .availableBalance(account.getAvailableBalance())
            .status(account.getStatus())
            .createdAt(account.getCreatedAt())
            .updatedAt(account.getUpdatedAt())
            .build();
    }

    /**
     * Demo/simülasyon amaçlı hesap numarası üretir.
     * Gerçek bankacılık sisteminde bu, merkezi bir numaralandırma servisi ile yapılır.
     */
    private String generateAccountNumber() {
        return String.format("%010d", (long) (Math.random() * 10_000_000_000L));
    }

    /**
     * Demo/simülasyon amaçlı IBAN üretir.
     * Format: TR + 2 kontrol hanesı + 24 rakam.
     * Bu yalnızca biçimsel geçerlilik sağlar, gerçek bir banka hesabı değildir.
     */
    private String generateIban() {
        StringBuilder sb = new StringBuilder("TR");
        for (int i = 0; i < 24; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }
}

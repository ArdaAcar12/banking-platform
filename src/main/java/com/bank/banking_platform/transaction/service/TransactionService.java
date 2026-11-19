package com.bank.banking_platform.transaction.service;

import com.bank.banking_platform.account.repository.AccountRepository;
import com.bank.banking_platform.transaction.dto.TransactionListResponse;
import com.bank.banking_platform.transaction.dto.TransactionResponse;
import com.bank.banking_platform.transaction.entity.FinancialTransaction;
import com.bank.banking_platform.transaction.entity.TransactionStatus;
import com.bank.banking_platform.transaction.entity.TransactionType;
import com.bank.banking_platform.transaction.repository.FinancialTransactionRepository;
import com.bank.banking_platform.transaction.repository.LedgerEntryRepository;
import com.bank.banking_platform.auth.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Finansal işlem sorgulama servisi.
 * <p>
 * Güvenlik kuralları:
 * <ul>
 *   <li>Kullanıcı yalnızca kendi hesaplarına ait işlemleri görebilir.</li>
 *   <li>Hesap sahipliği her istek altında veritabanı üzerinden doğrulanır.</li>
 *   <li>Ledger ve transaction kayıtları salt okunurdur; bu servis'te değiştirme yoktur.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private static final int MAX_PAGE_SIZE = 100;

    private final FinancialTransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final com.bank.banking_platform.account.repository.AccountRepository accountRepository;

    /**
     * Kullanıcının tüm hesaplarına ait işlemleri sayfalı olarak listeler.
     */
    public TransactionListResponse listTransactions(
        UUID userId,
        Integer page,
        Integer size,
        String sort,
        TransactionType type,
        TransactionStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        validatePagination(page, size);
        validateDateRange(startDate, endDate);

        Pageable pageable = buildPageable(page, size, sort);

        Page<FinancialTransaction> pageResult = resolveTransactionPage(
            userId, type, status, startDate, endDate, pageable
        );

        List<TransactionResponse> responses = pageResult.getContent().stream()
            .map(this::toResponse)
            .toList();

        return TransactionListResponse.builder()
            .data(responses)
            .totalElements(pageResult.getTotalElements())
            .totalPages(pageResult.getTotalPages())
            .page(pageResult.getNumber())
            .size(pageResult.getSize())
            .build();
    }

    /**
     * Belirli hesaba ait işlem geçmişini listeler.
     * Hesap sahipliği çağrıcısı tarafından önceden doğrulanmıştır.
     */
    public TransactionListResponse listTransactionsByAccount(
        UUID accountId,
        UUID userId,
        Integer page,
        Integer size,
        String sort,
        TransactionType type,
        TransactionStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        validatePagination(page, size);
        validateDateRange(startDate, endDate);

        Pageable pageable = buildPageable(page, size, sort);

        // Hesaba ait işlem ID'lerini topla
        List<UUID> transactionIds = ledgerEntryRepository.findTransactionIdsByAccountId(accountId);

        if (transactionIds.isEmpty()) {
            return TransactionListResponse.builder()
                .data(List.of())
                .totalElements(0L)
                .totalPages(0)
                .page(page)
                .size(size)
                .build();
        }

        // Kullanıcının kendi işlemlerini filtrele ve sayfala
        Page<FinancialTransaction> pageResult = transactionRepository.findByIdInAndInitiatedById(
            transactionIds, userId, pageable
        );

        List<TransactionResponse> responses = pageResult.getContent().stream()
            .map(this::toResponse)
            .toList();

        return TransactionListResponse.builder()
            .data(responses)
            .totalElements(pageResult.getTotalElements())
            .totalPages(pageResult.getTotalPages())
            .page(pageResult.getNumber())
            .size(pageResult.getSize())
            .build();
    }

    /**
     * Belirtilen ID'ye sahip işlem detayını döndürür.
     * Yalnızca işlem sahibi erişebilir.
     */
    public TransactionResponse getTransaction(UUID userId, UUID transactionId) {
        FinancialTransaction transaction = transactionRepository.findByIdAndInitiatedById(
            transactionId, userId
        );
        if (transaction == null) {
            throw new ResourceNotFoundException("İşlem bulunamadı.");
        }
        return toResponse(transaction);
    }

    private TransactionResponse toResponse(FinancialTransaction transaction) {
        return TransactionResponse.builder()
            .id(transaction.getId())
            .referenceNumber(transaction.getReferenceNumber())
            .transactionType(transaction.getTransactionType())
            .status(transaction.getStatus())
            .amount(transaction.getAmount())
            .currency(transaction.getCurrency())
            .description(transaction.getDescription())
            .createdAt(transaction.getCreatedAt())
            .completedAt(transaction.getCompletedAt())
            .build();
    }

    private Page<FinancialTransaction> resolveTransactionPage(
        UUID userId,
        TransactionType type,
        TransactionStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    ) {
        if (type != null && status != null && startDate != null && endDate != null) {
            return transactionRepository.findByInitiatedByIdAndTransactionTypeAndStatusAndCreatedAtBetween(
                userId, type, status, startDate, endDate, pageable
            );
        }
        if (type != null && status != null) {
            return transactionRepository.findByInitiatedByIdAndTransactionTypeAndStatusAndCreatedAtBetween(
                userId, type, status, null, null, pageable
            );
        }
        if (type != null) {
            return transactionRepository.findByInitiatedByIdAndTransactionType(
                userId, type, pageable
            );
        }
        if (status != null) {
            return transactionRepository.findByInitiatedByIdAndStatus(
                userId, status, pageable
            );
        }
        if (startDate != null && endDate != null) {
            return transactionRepository.findByInitiatedByIdAndCreatedAtBetween(
                userId, startDate, endDate, pageable
            );
        }
        return transactionRepository.findByInitiatedById(userId, pageable);
    }

    private void validatePagination(Integer page, Integer size) {
        if (page == null || page < 0) {
            throw new IllegalArgumentException("Sayfa numarası 0 veya daha büyük olmalıdır.");
        }
        if (size == null || size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                "Sayfa boyutu 1 ile " + MAX_PAGE_SIZE + " arasında olmalıdır."
            );
        }
    }

    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                "Başlangıç tarihi bitiş tarihinden sonra olamaz."
            );
        }
    }

    private PageRequest buildPageable(Integer page, Integer size, String sort) {
        Sort sortOrder = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split(",");
            String property = parts[0].trim();
            Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
            sortOrder = Sort.by(direction, property);
        }
        return PageRequest.of(page, size, sortOrder);
    }
}

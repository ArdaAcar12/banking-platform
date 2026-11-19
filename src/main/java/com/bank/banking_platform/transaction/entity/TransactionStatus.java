package com.bank.banking_platform.transaction.entity;

/**
 * Finansal işlem durumlarını tanımlar.
 * Veritabanında STRING olarak saklanır.
 */
public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REVERSED
}

package com.bank.banking_platform.transaction.entity;

/**
 * Finansal işlem türlerini tanımlar.
 * Veritabanında STRING olarak saklanır.
 */
public enum TransactionType {
    TRANSFER,
    DEPOSIT,
    WITHDRAWAL,
    FEE
}

package com.bank.banking_platform.transaction.entity;

/**
 * Ledger kaydı yönünü tanımlar.
 * DEBIT: borç kaydı (hesaptan düşülür).
 * CREDIT: alacak kaydı (hesaba eklenir).
 * Veritabanında STRING olarak saklanır.
 */
public enum LedgerDirection {
    DEBIT,
    CREDIT
}

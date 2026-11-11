package com.bank.banking_platform.auth.entity;

/**
 * Kullanıcı rollerini tanımlar.
 * Her rol, Spring Security yetkilendirmeinde {@code ROLE_} öneki ile yetki olarak kullanılır.
 */
public enum Role {
    CUSTOMER,
    ADMIN,
    SUPPORT
}

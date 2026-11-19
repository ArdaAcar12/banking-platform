package com.bank.banking_platform.account.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Hesap listesi yanıt sarmalayıcısı.
 * Mevcut API response standardına uygun olarak 'data' alanı ile döner.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountListResponse {

    private List<AccountResponse> data;
}

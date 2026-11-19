package com.bank.banking_platform.transaction.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Finansal işlem listesi yanıt sarmalayıcısı.
 * Mevcut API response standardına uygun olarak 'data' alanı ile birlikte
 * sayfalama meta verilerini içerir.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionListResponse {

    private List<TransactionResponse> data;
    private Long totalElements;
    private Integer totalPages;
    private Integer page;
    private Integer size;
}

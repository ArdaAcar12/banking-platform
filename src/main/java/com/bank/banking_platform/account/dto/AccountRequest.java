package com.bank.banking_platform.account.dto;

import com.bank.banking_platform.account.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Yeni hesap açma isteği.
 * <p>
 * Kullanıcı yalnızca hesap türü ve para birimini belirtebilir.
 * Bakiye, IBAN, hesap numarası gibi alanlar backend tarafından oluşturulur
 * ve bu istekle kabul edilmez.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotNull(message = "Hesap türü zorunludur.")
    private AccountType accountType;

    @NotBlank(message = "Para birimi zorunludur.")
    @Size(min = 3, max = 3, message = "Para birimi 3 harfli ISO 4217 kodu olmalıdır.")
    private String currency;
}

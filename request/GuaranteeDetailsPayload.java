package com.ing.bankguarantees.models.request;

import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.BankGuaranteeLanguage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuaranteeDetailsPayload<T> {

    @NotNull
    private BankGuaranteeCode bgCode;

    @NotNull
    private BankGuaranteeLanguage bgLanguage;

    @NotEmpty
    private BigDecimal bgAmount;

    @NotBlank
    private String bgTypesAmtCurrency;

    @NotNull
    private T bankGuarantee;

}
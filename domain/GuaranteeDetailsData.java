package com.ing.bankguarantees.models.domain;

import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.BankGuaranteeLanguage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuaranteeDetailsData<T> {
    private BankGuaranteeCode bgCode;

    private BankGuaranteeLanguage bgLanguage;

    private BigDecimal bgAmount;

    private String bgCurrency;

    private T bankGuarantee;

}
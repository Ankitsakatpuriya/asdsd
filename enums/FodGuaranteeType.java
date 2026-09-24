package com.ing.bankguarantees.models.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FodGuaranteeType {
    DCK("via an amount blocked by DCK","via an amount blocked by CDC","via an amount blocked by CDC"),
    FINANCIAL_INSTITUTION("via een andere financiële instelling", "via an other Financial Institution", "via une autre institution financière");

    private final String nlTranslation;
    private final String enTranslation;
    private final String frTranslation;

}

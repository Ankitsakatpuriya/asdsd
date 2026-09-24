package com.ing.bankguarantees.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BeneficiaryType {

    PRIVATE_INDIVIDUAL("Private Individual"),
    COMPANY("Company");

    private final String value;
}

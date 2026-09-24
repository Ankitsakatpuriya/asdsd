package com.ing.bankguarantees.models.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BankGuaranteeEndType {
    SPECIFIED,                           //("Specified"),
    UNSPECIFIED,                         //("Unspecified (with revocation clause)"),
    ACCEPTANCE_WITH_EXPIRY_DATE,         //("50% at provisional acceptance / 50% at final acceptance with an expiry date"),
    ACCEPTANCE_WITH_NO_EXPIRY_DATE,      //("50% at provisional acceptance / 50% at final acceptance with no expiry date"),
    OTHER;                               //("Other (e.g. reduction plan and partial releases)");
}

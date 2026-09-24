package com.ing.bankguarantees.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CoverType {

    CASH("With or Without Cash"),
    REMAINING_AMOUNT("Remaining Amount");

    private final String value;
}

package com.ing.bankguarantees.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PegaCaseType {
    NEW("New"),

    AMEND("Amendment-Increase");

    private final String description;
}


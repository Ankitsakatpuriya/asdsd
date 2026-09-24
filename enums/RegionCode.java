package com.ing.bankguarantees.models.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RegionCode {
    WALLONIA("Wallonia"),
    FLANDERS("Flanders"),
    FRANCE("France");

    private final String name;
}

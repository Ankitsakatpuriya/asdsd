package com.ing.bankguarantees.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BankGuaranteeDeliveryMode {

    EMAIL_ONLY("Email"),

    REGISTERED_POST("Registered post"),

    COURIER_SERVICE("Courier service");

    private final String description;

}
package com.ing.bankguarantees.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@AllArgsConstructor
@Getter
public enum DigitalAddressType {

    EMAIl("EMAIL_ADR"),
    PHONE_NUMBER("TEL_ADR");

    private final String type;

    public static Optional<DigitalAddressType> getByType(String type) {
        for (DigitalAddressType digitalAddressType : values()) {
            if (type.equalsIgnoreCase(digitalAddressType.getType()))
                return Optional.of(digitalAddressType);
        }
        return Optional.empty();
    }
}
package com.ing.bankguarantees.models.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor

public enum TransportOperatorActivityType {
    FORWARDING_AGENT("Forwarding Agent","Commissionnaire de transport", "Expediteur", 0),
    FORWARDING_BROKER("Forwarding Broker", "Courtier en transport", "Expeditiemakelaar",  1);

    private final String enTranslation;
    private final String frTranslation;
    private final String nlTranslation;

    private final int value;
}

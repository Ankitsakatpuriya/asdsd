package com.ing.bankguarantees.models.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum BankGuaranteeLanguage {

    ENGLISH("en"),
    FRENCH("fr"),
    DUTCH("nl"),
    GERMAN("de");

    private final String languageCode;
    private static final Set<String> SUPPORTED_LANGUAGES = Arrays.stream(values())
            .map(BankGuaranteeLanguage::getLanguageCode)
            .collect(Collectors.toUnmodifiableSet());

    public static boolean isSupportedLanguage(String languageCode) {
        return SUPPORTED_LANGUAGES.contains(languageCode.toLowerCase());
    }
}

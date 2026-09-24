package com.ing.bankguarantees.models.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentType {


    BG_DRAFT("FILLED_IN_CONCEPT", "Bank Guarantee Filled In Concept"),
    BG_FINAL("BG_FINAL_VERSION", "Bank Guarantee Final Version"),
    CONTRACT("CONTRACT_DRAFT", "Contract Letter Draft Version"),
    CONTRACT_FINAL("CONTRACT_FINAL", "Contract Letter Final Version"),
    CUSTOMIZED_DOC("CUSTOMIZED_DOC", "Bank Guarantee Custom Document");


    private final String name;
    private final String description;

}

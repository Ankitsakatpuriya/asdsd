package com.ing.bankguarantees.models.domain;

import lombok.Builder;

@Builder
public record DossierUpdateData(String requestId, String legalEntityId, String documentId) {
}
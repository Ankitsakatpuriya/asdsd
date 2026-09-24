package com.ing.bankguarantees.models.request;

import lombok.Builder;

@Builder
public record DossierUpdatePayload(String requestId, String legalEntityId, String documentId) {
}
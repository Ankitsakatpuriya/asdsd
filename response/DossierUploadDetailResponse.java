package com.ing.bankguarantees.models.response;

import lombok.Builder;

@Builder
public record DossierUploadDetailResponse(
        String documentId,
        String requestId,
        String dossierType,
        String dossierSubType,
        String documentType,
        String documentSubType,
        String documentName,
        String applicationOwnerId) {
}

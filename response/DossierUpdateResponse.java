package com.ing.bankguarantees.models.response;

import com.ing.bankguarantees.models.enums.CustomDocumentStatus;
import lombok.Builder;

@Builder
public record DossierUpdateResponse(String requestId, CustomDocumentStatus status) {
}
package com.ing.bankguarantees.models.domain;

import com.ing.bankguarantees.models.enums.CustomDocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Document entity - used to map the table DOCUMENT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomDocument {
    private String id;
    private String documentId;
    private CustomDocumentStatus status;
    private String requestId;
    private String dossierId;
    private String agreementDossierId;
    private String organisationId;
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
}

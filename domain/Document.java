package com.ing.bankguarantees.models.domain;

import com.ing.bankguarantees.models.enums.DocumentStatus;
import com.ing.bankguarantees.models.enums.DocumentType;
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
public class Document {
    private String id;
    private String documentId;
    private DocumentType documentType;
    private DocumentStatus status;
    private String requestId;
    private String organisationId;
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
}

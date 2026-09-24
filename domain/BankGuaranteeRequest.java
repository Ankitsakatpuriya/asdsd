package com.ing.bankguarantees.models.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankGuaranteeRequest {
    private String id;
    private String requestId;
    private BankGuaranteeRequestStatus status;
    private String sessionId;
    private String individualId;
    private String organisationId;
    private BankGuaranteeRequestData bgRequest;
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
    private String pegaCaseId;
    private String masterReferenceNumber;
}

package com.ing.bankguarantees.models.domain;

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
public class Reporting {
    private String id;
    private String requestId;
    private String stp;
    private BankGuaranteeRequestStatus status;
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;

}

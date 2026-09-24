package com.ing.bankguarantees.models.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CallbackDto {
    boolean intermediate;
    private String darUuid;
    private String agreementId;
    private String status;
    private List<String> operations;
    private String actorUuid;
}
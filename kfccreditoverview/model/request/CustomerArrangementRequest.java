package com.ing.bankguarantees.remote.rest.kfccreditoverview.model.request;


import lombok.Builder;

import java.util.List;

@Builder
public record CustomerArrangementRequest(
        List<PartyIdentifiersRequest> involvedPartyIdentifiers,
        EmployeeIdRequest employeeId,
        Integer level
) {

    @Builder
    public record PartyIdentifiersRequest(String type, String value) {
    }

    @Builder
    public record EmployeeIdRequest(String type, String value) {
    }

}

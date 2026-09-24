package com.ing.bankguarantees.remote.rest.involveparty.grantees.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssociatedPartyResponse {

    private String involvedPartyInvolvedPartyRelationshipType;

    @NotNull
    private List<InvPartyGrantorResponse> grantors;
    private InvPartyGranteeResponse grantee;
}

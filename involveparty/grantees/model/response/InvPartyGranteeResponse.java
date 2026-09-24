package com.ing.bankguarantees.remote.rest.involveparty.grantees.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvPartyGranteeResponse {
    @NotNull
    private String id;
    private String type;
    private String involvedPartyType;
}

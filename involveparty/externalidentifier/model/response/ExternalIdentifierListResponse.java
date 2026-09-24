package com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalIdentifierListResponse {
    @NotNull
    private List<ExternalIdentifierResponse> involvedPartyExternalIdentifiers;
}


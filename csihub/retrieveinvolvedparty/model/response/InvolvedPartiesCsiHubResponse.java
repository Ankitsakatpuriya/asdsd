package com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvolvedPartiesCsiHubResponse {
    @NotNull
    private InvolvedPartyCsiResponse involvedParty;
}

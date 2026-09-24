package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DossierDataResponse {

    @NotNull
    private String id;


}

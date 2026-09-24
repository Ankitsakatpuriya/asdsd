package com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationNameCsiResponse {
    private int nameUsage;
    @NotBlank
    private String fullName;
    private int language;
}

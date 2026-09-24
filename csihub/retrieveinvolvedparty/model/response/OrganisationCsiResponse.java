package com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationCsiResponse {
    private List<OrganisationNameCsiResponse> organisationName;
}

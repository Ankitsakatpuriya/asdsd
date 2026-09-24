package com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * RequestRetrieveInvolvedParty
 */

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CSIHubInvolvedPartiesRequest {

    private InvolvedPartyCsiRequest involvedParty;

}




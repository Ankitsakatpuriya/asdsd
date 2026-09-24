package com.ing.bankguarantees.models;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DossierRequestData {

    private String requestId;
    private String legalEntityId;
    private AccessToken accessToken;
}


package com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.model.response;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalIdentifierResponse {

    @NotBlank
    private String involvedPartyExternalIdentifierType;

    @NotBlank
    private String involvedPartyExternalIdentifierValue;

}


package com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAddressContactPointUsageCsiResponse {
    private EmailAddressCsiResponse emailAddress;
}

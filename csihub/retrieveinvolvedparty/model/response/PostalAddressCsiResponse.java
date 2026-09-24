package com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostalAddressCsiResponse {
    private String firstAddressLine;
    private String streetName;
    private int firstHouseNumber;
    private String countryCode;
    private int postalCode;
    private String city;

}

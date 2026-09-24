package com.ing.bankguarantees.remote.rest.referencedata.attributes.model.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceDataAttributeRequest {

    private String tableDistributionName;
    private List<String> column;

}

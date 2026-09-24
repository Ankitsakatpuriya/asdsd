package com.ing.bankguarantees.remote.rest.referencedata.attributes.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceDataAttributeOutput {

    @NotNull
    private List<String> businessKeyList;

}

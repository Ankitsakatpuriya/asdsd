package com.ing.bankguarantees.models.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatoriesSelectionRequest {

    @NotBlank
    private String legalEntityId;

    @NotNull
    private GuaranteeDetailsData<?> guaranteeDetails;

}

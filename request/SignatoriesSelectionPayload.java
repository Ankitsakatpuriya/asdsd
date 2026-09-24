package com.ing.bankguarantees.models.request;

import com.ing.bankguarantees.validation.annotation.ValidSignatorySelection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;


@Data
@Builder
@Validated
@NoArgsConstructor
@AllArgsConstructor
@ValidSignatorySelection
public class SignatoriesSelectionPayload {

    @NotBlank
    private String legalEntityId;

    @NotNull
    private GuaranteeDetailsPayload<?> guaranteeDetails;

}

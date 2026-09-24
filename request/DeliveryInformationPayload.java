package com.ing.bankguarantees.models.request;

import com.ing.bankguarantees.models.enums.BankGuaranteeDeliveryMode;
import com.ing.bankguarantees.models.enums.BankGuaranteeRecipient;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryInformationPayload {

    @NotNull
    private BankGuaranteeRecipient recipient;

    @NotNull
    private BankGuaranteeDeliveryMode mode;

}
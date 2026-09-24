package com.ing.bankguarantees.models.domain;

import com.ing.bankguarantees.models.enums.BankGuaranteeDeliveryMode;
import com.ing.bankguarantees.models.enums.BankGuaranteeRecipient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryInformationData {
    private BankGuaranteeRecipient recipient;
    private BankGuaranteeDeliveryMode mode;

}
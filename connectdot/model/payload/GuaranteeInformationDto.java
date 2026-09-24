package com.ing.bankguarantees.remote.rest.connectdot.model.payload;

import com.ing.bankguarantees.remote.rest.connectdot.model.payload.ContractDocumentPayload.ContractDetailPayload;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.ContractDocumentPayload.BankGuaranteeDetailPayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GuaranteeInformationDto {
    private ContractDetailPayload contractDetailPayload;
    private String bankGuaranteeDescription;
    private String bankAccountCredit;
}

package com.ing.bankguarantees.remote.rest.connectdot.model.request;


import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.enums.CreditType;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectDotInput {

    private BankGuaranteeRequestData bankGuaranteeRequestData;
    private String documentId;
    private DocumentType documentType;
    private CreditType creditType;
    private GarOutput garOutput;
    private String masterReferenceId;

}

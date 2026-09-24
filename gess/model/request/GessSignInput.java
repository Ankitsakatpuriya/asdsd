package com.ing.bankguarantees.remote.rest.gess.model.request;


import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.BankGuaranteeLanguage;
import com.ing.bankguarantees.models.enums.DocumentType;
import lombok.Builder;

import java.math.BigInteger;

@Builder
public record GessSignInput(

        String requesterEmail,
        BigInteger requesterId,
        String requesterSystemId,
        String documentBase64,
        String fileName,
        DocumentType documentType,
        BankGuaranteeCode bankGuaranteeCode,
        BankGuaranteeLanguage bankGuaranteeLanguage
) {
}

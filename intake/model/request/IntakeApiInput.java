package com.ing.bankguarantees.remote.rest.intake.model.request;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.remote.rest.intake.IntakeApiProperties;
import lombok.Builder;

import java.util.List;

@Builder
public record IntakeApiInput(

        BankGuaranteeRequest bankGuaranteeRequest,
        List<Document> documents
) {
}

package com.ing.bankguarantees.remote.rest.pega.model;


import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.models.enums.PegaCaseType;
import lombok.Builder;

import java.util.List;


@Builder
public record PegaCreateCaseInput(BankGuaranteeRequest bankGuaranteeRequest, List<Document> documents,
                                  PegaCaseType pegaCaseType) {
}

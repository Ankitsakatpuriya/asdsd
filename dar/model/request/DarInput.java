package com.ing.bankguarantees.remote.rest.dar.model.request;

import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DarInput {

    private BankGuaranteeRequestData bankGuaranteeRequestData;
    private List<Document> documents;
}

package com.ing.bankguarantees.models.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FulfilmentInput {
    private BankGuaranteeRequest bankGuaranteeRequest;
    private List<Document> documents;
}

package com.ing.bankguarantees.models.domain;

import com.ing.bankguarantees.models.Identifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class InvolvePartyData {

    private String legalForm;
    private boolean wbCustomer;
    private List<Identifier> internalIdentifiers;


}

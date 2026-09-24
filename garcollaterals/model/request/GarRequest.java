package com.ing.bankguarantees.remote.rest.garcollaterals.model.request;

import com.ing.bankguarantees.models.Identifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GarRequest {
    private String language;
    private List<Identifier> involvedPartyIdentifiers;
}

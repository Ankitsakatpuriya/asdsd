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
public class LegalRepresentativeData {
    private String uuid;
    private boolean firstSigner;
    private boolean signer;
    private String fullName;
    private String emailId;
    private Integer signingPower;
    private boolean active;
    private String preferredLanguage;
    private List<Identifier> internalIdentifiers;
}

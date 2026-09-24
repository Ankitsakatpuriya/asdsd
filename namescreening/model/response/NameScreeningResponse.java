package com.ing.bankguarantees.remote.rest.namescreening.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameScreeningResponse {
    private String hitsNumber;
    @NotNull
    private List<HitsList> hitsList;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HitsList {
        private String partyForAMLFieldWithHit;
        private String amlDecisionalListUsed;
        private String amlOriginListUsed;
        private String entryId;
        private String entryValue;
        private String entryAttribute;
        private String hitFoundOnAlias;
        private String matchingPercentage;
        private String sensitivity;
        private String priority;
        private String partyId;
        private String partyType;
        private String fullName;
        private String firstName;
        private String lastName;
        private String countryCode;
        private String countryDescription;
    }
}

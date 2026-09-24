package com.ing.bankguarantees.remote.rest.pamqualification.model.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PamQualificationResponse {

    @Valid
    private PartyResponse beneficiary;


    @Getter
    public enum EfcValuesEnum {
        ECO01,
        ECO02,
        ECO6,
        ECO8,
        ECO11,
        ECO15,
        ECO16,
        ECO31,
        ECO32,
        ECO82,
        ECO83,
        ECO89,
        ECO12,
        ECO13,
        ECO14
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartyResponse {
        @NotNull
        private String status;
        private List<SubResult> subResults;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubResult {
        private String name;
        private String status;
        private List<Reason> reasons;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reason {
        private Integer code;
        private String description;
        private EfcValuesEnum efc;
        private String result;
    }
}

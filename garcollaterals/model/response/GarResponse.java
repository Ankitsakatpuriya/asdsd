package com.ing.bankguarantees.remote.rest.garcollaterals.model.response;

import com.ing.bankguarantees.models.Identifier;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NotNull
@NoArgsConstructor
@AllArgsConstructor
public class GarResponse {
    @Valid
    private List<GarResponseDetails> garResponse;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GarResponseDetails {
        @NotNull
        @Valid
        private Company company;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Company {
            @NotNull
            private List<@Valid Identification> identification;
            @NotNull
            private List<@Valid CollateralResponse> collaterals;
            @NotNull
            private List<@Valid CovenantResponse> covenants;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Identification {
                @NotNull
                private String identifier;
                @NotNull
                private String name;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            public static
            class CollateralResponse {
                @NotNull
                private String collateralType;
                private Identifier involvedPartyIdentifier;
                private String generalCoverType;
                private String assetDescription;
                private String comments;
                private CollateralValue collateralValue;
                private RegistrationDate registrationDate;
            }

            @Data
            @AllArgsConstructor
            @NoArgsConstructor
            public static class CovenantResponse {
                @NotNull
                private String collateralType;
                private Identifier involvedPartyIdentifier;
                private String generalCoverType;
                private CollateralValue collateralValue;
                private RegistrationDate registrationDate;
                private String comments;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class CollateralValue {
                private BigDecimal value;
                private Currency currency;

                @Data
                @NoArgsConstructor
                @AllArgsConstructor
                public static class Currency {
                    private String code;
                }
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class RegistrationDate {

                private String coverRegistrationDate;
            }
        }
    }
}

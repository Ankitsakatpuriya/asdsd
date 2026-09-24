package com.ing.bankguarantees.remote.rest.productagreement.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductAgreementResponse {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss[.SSS]'Z'";

    @NotNull
    @Valid
    private ProductAgreementsResponse productAgreements;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductAgreementsResponse {

        @NotNull
        @Valid
        private List<ProductAgreementDetailsResponse> data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductAgreementDetailsResponse {

        private String productType;
        private String name;
        private String lifeCycleStatusType;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ProductAgreementResponse.DATE_TIME_FORMAT)
        private LocalDateTime effectiveDate;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ProductAgreementResponse.DATE_TIME_FORMAT)
        private LocalDateTime endDate;
        @Valid
        private List<IdentifierResponse> identifiers;
        private String currency;
        private InvolvedPartyRelationships involvedPartyRelationships;

        public ProductAgreementDetailsResponse(String productType, String name, String lifeCycleStatusType,
                                               LocalDateTime effectiveDate, LocalDateTime endDate,
                                               List<IdentifierResponse> identifiers, String currency) {
            this.productType = productType;
            this.name = name;
            this.lifeCycleStatusType = lifeCycleStatusType;
            this.effectiveDate = effectiveDate;
            this.endDate = endDate;
            this.identifiers = identifiers;
            this.currency = currency;
        }

        public boolean isActive() {
            return isActiveFunc().test(effectiveDate, endDate);
        }

        private static BiPredicate<LocalDateTime, LocalDateTime> isActiveFunc() {
            return (effectiveDate, endDate) -> {
                if (effectiveDate == null) {
                    return false;
                }
                LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
                return now.isBefore(Optional.ofNullable(endDate).orElse(LocalDateTime.MAX)) && now
                        .isAfter(effectiveDate);
            };
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdentifierResponse {
        @NotNull
        private String type;
        @NotNull
        private String value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvolvedPartyRelationships {
        List<InvolvedPartyRelationship> data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvolvedPartyRelationship {
        @NotNull
        InvolvedParty involvedParty;
        @NotNull
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvolvedParty {
        @NotNull
        private String id;
    }


}

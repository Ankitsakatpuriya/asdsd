
package com.ing.bankguarantees.models.request;

import com.ing.bankguarantees.validation.annotation.ValidCbeNumber;
import com.ing.bankguarantees.validation.annotation.ValidDigitalAddress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Builder
@Validated
@NoArgsConstructor
@AllArgsConstructor
public class InstructingPartyPayload {

    @NotNull
    private IndividualPayload individual;
    @NotNull
    private OrganisationPayload organisation;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganisationPayload {

        @NotBlank
        private String legalEntityId;

        @ValidCbeNumber
        private String cinNumber;

        @NotNull
        private OrganisationNamePayload organisationName;
        @NotNull
        private PostalAddressPayload postalAddress;

        @Valid
        @NotEmpty
        private List<@ValidDigitalAddress DigitalAddressPayload> digitalAddresses;
    }

    @Data
    @Builder
    @Validated
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndividualPayload {

        @NotBlank
        private String legalRepId;
        @NotNull
        private IndividualNamePayload individualName;

        @Valid
        @NotEmpty
        private List<@ValidDigitalAddress DigitalAddressPayload> digitalAddresses;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndividualNamePayload {
        private String firstName;
        private String lastName;
        private String fullName;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostalAddressPayload {

        @NotBlank
        private String countryCode;

        private String cityName;

        private String postalCode;

        private String firstAddress;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganisationNamePayload {

        private String type;

        private String fullName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DigitalAddressPayload {

        private String digitalAddressType;
        private String digitalAddressUsageType;
        private String fullDigitalAddress;
    }
}


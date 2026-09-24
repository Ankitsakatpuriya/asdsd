package com.ing.bankguarantees.remote.rest.intake.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import com.ing.bankguarantees.utils.ConstantUtils;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record ApplicationRequest(

        ObligorRequest obligor,
        ApplicantRequest applicant,
        BeneficiaryRequest beneficiary,
        List<DocumentsRequest> documents,

        @JsonProperty("undertakingApplicationDetails")
        UndertakingApplicationsDetailsRequest undertakingApplications,

        DeliveryChannelRequest deliveryChannel,
        String masterReferenceId,
        String isStp,
        String isoIndicName,
        String team,
        String guaranteeCreditMode,
        String advisingPartyIndicator,
        String counterUndertakingIndicator,
        String garCover

) {

    @Builder
    public record UndertakingApplicationsDetailsRequest(
            String purpose,
            String termsAndConditions,
            UndertakingTypeRequest type,
            String applicantReferenceNumber,
            ExpiryDetailsRequest expiryDetails,
            UndertakingAmountRequest undertakingAmount,
            UndertakingWordingRequest undertakingWording,
            ObligorChargeAccountRequest obligorChargeAccount,
            UnderlyingTransactionRequest underlyingTransaction,
            ObligorLiabilityAccountRequest obligorLiabilityAccount

    ) {
    }

    @Builder
    public record BeneficiaryRequest(
            String name,
            PostalAddressRequest postalAddress,
            ContactDetailsRequest contactDetails
    ) {
    }

    @Builder
    public record ApplicantRequest(
            String name,
            PostalAddressRequest postalAddress,
            ContactDetailsRequest contactDetails,
            OrganisationRequest organisation
    ) {
    }

    @Builder
    public record ObligorRequest(
            String name,
            PostalAddressRequest postalAddress,
            ContactDetailsRequest contactDetails,
            OrganisationRequest organisation
    ) {
    }

    @Builder
    public record DocumentsRequest(String id, String type) {
    }

    @Builder
    public record UndertakingAmountRequest(double amount, String currency) {
    }

    @Builder
    public record ExpiryDetailsRequest(ExpiryTermsRequest expiryTerms) {
    }

    @Builder
    public record ExpiryTermsRequest(

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConstantUtils.DATE_FORMAT)
            @JsonDeserialize(using = LocalDateDeserializer.class)
            @JsonSerialize(using = LocalDateSerializer.class)
            LocalDate date,

            String openEndedIndicator
    ) {
    }

    @Builder
    public record ObligorChargeAccountRequest(String currency, ObligorIdentificationRequest identification) {
    }

    @Builder
    public record UndertakingTypeRequest(String code) {
    }

    @Builder
    public record UnderlyingTransactionRequest(

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConstantUtils.DATE_FORMAT)
            @JsonDeserialize(using = LocalDateDeserializer.class)
            @JsonSerialize(using = LocalDateSerializer.class)
            LocalDate transactionDate,
            String identification,
            UndertakingAmountRequest transactionAmount

    ) {

    }

    @Builder
    public record UndertakingWordingRequest(String modelForm, WordingLanguageRequest requestedWordingLanguage) {
    }

    @Builder
    public record WordingLanguageRequest(String code) {
    }

    @Builder
    public record ObligorLiabilityAccountRequest(String currency, ObligorIdentificationRequest identification) {
    }

    @Builder
    public record ObligorIdentificationRequest(String iban) {
    }


    @Builder
    public record PostalAddressRequest(String addressLine, String postCode, String townName, String country) {
    }

    @Builder
    public record ContactDetailsRequest(String name, String phoneNumber, String emailAddress) {
    }

    @Builder
    public record DeliveryChannelRequest(String deliverToPartyType) {
    }

    @Builder
    public record OrganisationRequest(String idType, String id, String name) {
    }

    @Builder
    public record UndertakingAmount(BigDecimal amount, String currency) {
    }
}

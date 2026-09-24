package com.ing.bankguarantees.remote.rest.connectdot.model.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ing.bankguarantees.models.enums.CreditType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.ing.bankguarantees.utils.ConstantUtils.DATE_FORMAT;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ContractDocumentPayload extends BaseDocumentPayload {

    private CreditType creditType;
    private String correlationId;
    private String customerGender;
    private String customerBusiness;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate certificateCreationDate;
    private String coupures;

    @JsonProperty("depositdate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate depositDate;

    @JsonProperty("depositzipcode")
    private String depositZipCode;

    @JsonProperty("depositcity")
    private String depositCity;

    private String name;
    private String surname;
    private String subType;
    private String bankAccountDebit;
    private String bankAccountCredit;
    private String bankAccountBooking;
    private String ratePerYear;
    private String periodicity;
    private Double minimumPerRecord;
    private ContractDetailPayload contractDetails;
    private BankGuaranteeDetailPayload bankGuaranteeDetails;
    private ApplicantDocumentPayload applicant;

    @JsonProperty("benificiary")
    private BeneficiaryDocumentPayload beneficiary;

    private RepresentPayload representedBy;

    @JsonProperty("collaterals")
    private List<CollateralPayload> collateralPayloadList;
    @JsonProperty("covenants")
    private List<CovenantPayload> covenantPayloadList;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractDetailPayload {

        @JsonProperty("contractrefer")
        private String contractReference;
        private String contractDescription;

        @JsonProperty("contractValuta")
        private String contractCurrency;
        private Double contractAmount;
        private String contractPercentage;
        private String amountPercentage;
        private String contractType;

        private String bgReferenceNumber;
        private String licenseNumber;
        private String fodGuarantee;
        private String numberOfVehicles;
        private String profession;
        private String license;


        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonProperty("contractSignDt")
        private LocalDate contractSignDate;

        @JsonProperty("contractEndDt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate contractEndDate;
        private String contractStreet;
        private String contractZip;
        private String contractCity;
        private String contractCountry;
        private String lotsDescription;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate woodContractEndDateA;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate woodContractEndDateB;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate bgMaturityDate;

        @JsonProperty("bgexpiryCode")
        private String expiryCode;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate contractDueDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate contractValidityEndDate;

        private Double remainingAmount;
        private String replacePromiseId;
        private Double cash;
        private Double trancheOne;
        private Double trancheTwo;
        private Double trancheThree;
        private Double trancheFour;
        private String comments;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate deadlineOne;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate deadlineTwo;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate deadlineThree;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate deadlineFour;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate woodValidityEndDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate promiseEndDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankGuaranteeDetailPayload {

        private String bgLang;
        private Double bgAmount;
        private Double contractCreditAmount;

        @JsonProperty("bgValuta")
        private String bgCurrency;

        private String bgExpiryDescription;
        private String sendTo;
        private String sendVia;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepresentPayload {

        private String legalRep1;
        private String legalRep2;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollateralPayload {

        private String collateralType;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate coverRegistrationDate;

        private String currency;
        private BigDecimal collateralValue;
        private String grantedBy;
        private String generalCoverType;
        private String assetDescription;
        private String comments;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CovenantPayload {

        private String covenants;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate coverRegistrationDate;

        private String grantedBy;
        private String generalCoverType;
        private String comments;

    }

}

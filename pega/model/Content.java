package com.ing.bankguarantees.remote.rest.pega.model;

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
public record Content(

        @JsonProperty("VATNumber")
        String vatNumber,

        @JsonProperty("KNumber")
        String kNumber,

        @JsonProperty("Customer")
        String customer,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConstantUtils.PEGA_REQ_DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonProperty("DateIn")
        LocalDate dateIn,

        @JsonProperty("TempDocumentList")
        List<TempDocumentListItem> tempDocumentList,

        @JsonProperty("CurrencyCode")
        String currencyCode,

        @JsonProperty("Currency")
        String currency,

        @JsonProperty("DecisionSheetNeeded")
        String decisionSheetNeeded,

        @JsonProperty("DecisionSheetReceived")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConstantUtils.PEGA_DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        LocalDate decisionSheetReceived,

        @JsonProperty("AgreementDossierID")
        String agreementDossierId,

        @JsonProperty("BranchName")
        String branchName,

        @JsonProperty("IndirectGuarantee")
        String indirectGuarantee,

        @JsonProperty("GridID")
        String gridId,

        @JsonProperty("ExistingCreditline")
        String existingCreditline,

        @JsonProperty("Urgent")
        String urgent,

        @JsonProperty("APICaseID")
        String apiCaseID,

        @JsonProperty("Amount")
        BigDecimal amount,

        @JsonProperty("RequestDossierID")
        String requestDossierId,

        @JsonProperty("CrossBorder")
        boolean crossBorder,

        @JsonProperty("ApplicantEmailAddress")
        String applicantEmailAddress,

        @JsonProperty("ChannelName")
        String channelName,

        @JsonProperty("GuaranteeType")
        String guaranteeType,

        @JsonProperty("BeneficiaryEmailAddress")
        String beneficiaryEmailAddress,

        @JsonProperty("TypeOfSubmission")
        String typeOfSubmission,

        @JsonProperty("StandardOrCustom")
        String standardOrCustom,

        @JsonProperty("Country")
        String country,

        @JsonProperty("pyLabel")
        String pyLabel,

        @JsonProperty("CustomerAcctNumber")
        String customerAcctNumber,

        @JsonProperty("RequestType")
        String requestType,

        @JsonProperty("CashPledgeAccount")
        String cashPledgeAccount,

        @JsonProperty("Team")
        String team,

        @JsonProperty("ORFAgreementReceived")
        String OrfAgreementReceived,

        @JsonProperty("ExtraInfo")
        ExtraInfo extraInfo,

        @JsonProperty("CreditRiskCheck")
        String creditRiskCheck

        ) {
    @Builder
    public record TempDocumentListItem(

            @JsonProperty("DocumentID")
            String documentId,

            @JsonProperty("Label")
            String label,

            @JsonProperty("DocumentType")
            String documentType
    ) {
    }

            @Builder
            public record ExtraInfo(

                    @JsonProperty("Decision")
                    Decision decision
            ) {
                    @Builder
                    public record Decision(

                            @JsonProperty("PDLResult")
                            String pdlResult,

                            @JsonProperty("CreditLineResult")
                            String creditLineResult
                    ) {
                    }
            }

}
package com.ing.bankguarantees.remote.rest.pega.mapper;

import com.ing.bankguarantees.models.Identifier;
import com.ing.bankguarantees.models.domain.*;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.CreditLineStatus;
import com.ing.bankguarantees.remote.rest.pega.PegaProperties;
import com.ing.bankguarantees.remote.rest.pega.model.Content;
import com.ing.bankguarantees.remote.rest.pega.model.Content.TempDocumentListItem;
import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseInput;
import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseRequest;
import com.ing.bankguarantees.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.ing.bankguarantees.models.enums.StpCriteriaType.*;
import static com.ing.bankguarantees.utils.CommonUtils.getIdentifierValue;
import static com.ing.bankguarantees.utils.ConstantUtils.GRID_TYPE;


@Slf4j
@Component
@RequiredArgsConstructor
public class PegaRequestMapper {


    private static final String PEGA_PERFORMANCE_BOND_TYPE = "Performance Bond Guarantee";
    private static final String PEGA_RENTAL_TYPE = "Rental Guarantee";
    private static final String PEGA_ADVANCE_PAYMENT_TYPE = "Advance Payment Guarantee";
    private static final String PEGA_PAYMENT_GUARANTEE_TYPE = "Payment Guarantee";
    private static final String PEGA_RETENTION_TYPE = "Retention Guarantee";
    private static final String PEGA_BID_BOND_TYPE = "Bid Bond Guarantee";
    private static final String PEGA_CUSTOM_TYPE = "Customs Guarantee";
    private static final String OK = "OK";
    private static final String NOK = "NOK";
    private static final String YES = "Yes";
    private static final String NO = "No";


    private final PegaProperties pegaProperties;

    public PegaCreateCaseRequest preparePegaRequest(PegaCreateCaseInput pegaCreateCaseInput) {
        return PegaCreateCaseRequest.builder().caseTypeId(pegaProperties.getCaseTypeId())
                .processId(pegaProperties.getProcessId())
                .content(prepareContent(pegaCreateCaseInput))
                .build();
    }

    private Content prepareContent(PegaCreateCaseInput pegaCreateCaseInput) {
        BankGuaranteeRequest bankGuaranteeRequest = pegaCreateCaseInput.bankGuaranteeRequest();
        BankGuaranteeRequestData bankGuaranteeRequestData = bankGuaranteeRequest.getBgRequest();
        return Content.builder()
                .dateIn(LocalDate.now())
                .typeOfSubmission(pegaProperties.getTypeOfSubmission())
                .currencyCode(pegaProperties.getCurrency())
                .currency(pegaProperties.getCurrency())
                .branchName(pegaProperties.getBranchName())
                .indirectGuarantee(pegaProperties.getIndirectGuarantee())
                .urgent(pegaProperties.getTypeUrgent())
                .crossBorder(pegaProperties.getCrossBorder())
                .channelName(pegaProperties.getChannelName())
                .standardOrCustom(pegaProperties.getTypeStandard())
                .pyLabel(pegaProperties.getPyLabel())
                .requestType(pegaCreateCaseInput.pegaCaseType().getDescription())
                .team(pegaProperties.getTeam())
                .OrfAgreementReceived(pegaProperties.getOrfAgreementReceived())
                .decisionSheetNeeded(isDecisionSheetNeeded(bankGuaranteeRequest.getBgRequest().getStpResultDataSet()).orElse(null))
                .decisionSheetReceived(LocalDate.now().minusDays(10))
                .apiCaseID(bankGuaranteeRequestData.getReferenceNumber())
                .gridId(getGridId(bankGuaranteeRequestData.getInstructingParty().getOrganisation().getInternalIdentifiers()))
                .existingCreditline(isExistingCreditLine(bankGuaranteeRequest.getBgRequest().getStpResultDataSet()))
                .amount(bankGuaranteeRequestData.getGuaranteeDetails().getBgAmount())
                .agreementDossierId(bankGuaranteeRequestData.getDossierInformation().getAgreementDossierResponseId())
                .requestDossierId(bankGuaranteeRequestData.getDossierInformation().getDossierResponseId())
                .applicantEmailAddress(getAppEmailAddress(bankGuaranteeRequestData))
                .guaranteeType(getGuaranteeType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode()))
                .beneficiaryEmailAddress(bankGuaranteeRequest.getBgRequest().getBeneficiary().getEmailAddress().getEmailIdInformation())
                .vatNumber(bankGuaranteeRequestData.getInstructingParty().getOrganisation().getCinNumber())
                .country(bankGuaranteeRequest.getBgRequest().getInstructingParty().getOrganisation().getPostalAddress().getCountryCode())
                .customerAcctNumber(bankGuaranteeRequest.getBgRequest().getFinancialInformation().getAccountToBeDebited().getIbanNumber())
                .customer(bankGuaranteeRequestData.getInstructingParty().getOrganisation().getOrganisationName().getFullName())
                .tempDocumentList(getTempDocumentListItem(pegaCreateCaseInput.documents()))
                .extraInfo(Content.ExtraInfo.builder()
                        .decision(Content.ExtraInfo.Decision.builder()
                                .pdlResult(getPdlResult(bankGuaranteeRequest.getBgRequest().getStpResultDataSet()))
                                .creditLineResult(getCreditLineResult(bankGuaranteeRequest.getBgRequest().getStpResultDataSet()))
                                .build()).build())
                .creditRiskCheck(getCreditRiskCheck(bankGuaranteeRequest.getBgRequest().getStpResultDataSet()))
                .build();
    }

    private String getGuaranteeType(BankGuaranteeCode bgCode) {
        return switch (bgCode) {
            case PERFORMANCE_BOND, PUBLIC_CONTRACT, STATE_LOTTERY, OVAM, DCK_CDC,
                 GOODS_TRANSPORT, PASSENGER_TRANSPORT, OPERATORS_TRANSPORT, CUSTOMIZED_TEXT ->
                    PEGA_PERFORMANCE_BOND_TYPE;
            case CUSTOM_1, CUSTOM_2, CUSTOM_4, CUSTOM_5 -> PEGA_CUSTOM_TYPE;
            case PAYMENT_GUARANTEE, REAL_ESTATE -> PEGA_PAYMENT_GUARANTEE_TYPE;
            case ADVANCE_PAYMENT -> PEGA_ADVANCE_PAYMENT_TYPE;
            case MONEY_RETENTION_BOND -> PEGA_RETENTION_TYPE;
            case BID_BOND -> PEGA_BID_BOND_TYPE;
            case RENTAL, WOODS_PROM_A, WOODS_PROM_B, WOODS_PROM_VLA, WOODS_BGWAL_PUBLIC, WOODS_BG_PRIVATE,
                 WOODS_BG_DISCHARGE, WOODS_BGVLA_PUBLIC -> PEGA_RENTAL_TYPE;
            case ABSTRACT_PROM -> PEGA_RENTAL_TYPE;
            case PUBLIC_CONTRACT_PROM -> PEGA_RENTAL_TYPE;
        };

    }

    private String isExistingCreditLine(StpResultDataSet stpResult) {
        boolean sdsResponseStatus = stpResult.getStpResultByType(SDS_RESPONSE).map(StpResultDataSet.STPResultData::isStpPossible).orElse(false);
        var creditLineStatus = stpResult.getStpResultByType(CREDIT_LINE_BALANCE).map(StpResultDataSet.STPResultData::getJustification)
                .map(CreditLineStatus::valueOf).orElse(CreditLineStatus.NOT_AVAILABLE);
        return creditLineStatus == CreditLineStatus.NOT_AVAILABLE && sdsResponseStatus
                ? NO
                : YES;
    }

    private String getPdlResult(StpResultDataSet stpResult) {
        return stpResult.getStpResultByType(SDS_RESPONSE).map(StpResultDataSet.STPResultData::isStpPossible).orElse(false)
                ? OK
                : NOK;
    }

    private String getCreditRiskCheck(StpResultDataSet stpResult) {
        boolean beneficiaryNameScreeningStatus = stpResult.getStpResultByType(BENEFICIARY_NAME_SCREENING).map(StpResultDataSet.STPResultData::isStpPossible).orElse(false);
        boolean applicantNameScreeningStatus = stpResult.getStpResultByType(APPLICANT_NAME_SCREENING).map(StpResultDataSet.STPResultData::isStpPossible).orElse(false);
        boolean pamQualificationResponseStatus = stpResult.getStpResultByType(INSTRUCTING_PARTY_CDD).map(StpResultDataSet.STPResultData::isStpPossible).orElse(false);

        return beneficiaryNameScreeningStatus && applicantNameScreeningStatus && pamQualificationResponseStatus
                ? OK
                : NOK;
    }

    private Optional<String> isDecisionSheetNeeded(StpResultDataSet stpResult) {
        boolean sdsResponseStatus = stpResult.getStpResultByType(SDS_RESPONSE).map(StpResultDataSet.STPResultData::isStpPossible).orElse(false);
        var creditLineStatus = stpResult.getStpResultByType(CREDIT_LINE_BALANCE).map(StpResultDataSet.STPResultData::getJustification)
                .map(CreditLineStatus::valueOf).orElse(CreditLineStatus.NOT_AVAILABLE);
        return creditLineStatus == CreditLineStatus.SUFFICIENT_BALANCE || (creditLineStatus == CreditLineStatus.NOT_AVAILABLE && sdsResponseStatus)
                ? Optional.of(NO)
                : Optional.of(YES);
    }

    private String getCreditLineResult(StpResultDataSet stpResult) {
        return stpResult.getStpResultByType(CREDIT_LINE_BALANCE).map(StpResultDataSet.STPResultData::isStpPossible).orElse(false)
                ? OK
                : NOK;
    }

    private String getAppEmailAddress(BankGuaranteeRequestData bankGuaranteeRequestData) {
        return CommonUtils.getEmailDigitalAddress(bankGuaranteeRequestData.getInstructingParty().getIndividual().getDigitalAddresses());
    }

    private List<TempDocumentListItem> getTempDocumentListItem(List<Document> documentList) {
        return documentList
                .stream()
                .map(document -> TempDocumentListItem.builder()
                        .documentId(document.getDocumentId())
                        .documentType(document.getDocumentType().name())
                        .label(document.getDocumentType().name())
                        .build())
                .toList();
    }

    private String getGridId(List<Identifier> internalIdentifiers) {
        return getIdentifierValue(internalIdentifiers, List.of(GRID_TYPE))
                .map(Identifier::getValue)
                .orElse(null);
    }
}
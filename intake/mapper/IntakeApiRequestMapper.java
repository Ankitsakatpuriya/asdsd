package com.ing.bankguarantees.remote.rest.intake.mapper;

import com.ing.bankguarantees.models.Identifier;
import com.ing.bankguarantees.models.domain.*;
import com.ing.bankguarantees.models.domain.InstructingPartyData.IndividualData;
import com.ing.bankguarantees.models.domain.InstructingPartyData.OrganisationData;
import com.ing.bankguarantees.models.enums.*;
import com.ing.bankguarantees.models.guaranteetype.*;
import com.ing.bankguarantees.remote.rest.intake.IntakeApiProperties;
import com.ing.bankguarantees.remote.rest.intake.model.request.ApplicationRequest;
import com.ing.bankguarantees.remote.rest.intake.model.request.ApplicationRequest.*;
import com.ing.bankguarantees.remote.rest.intake.model.request.IntakeApiInput;
import com.ing.bankguarantees.remote.rest.intake.model.request.IntakeApiRequest;
import com.ing.bankguarantees.remote.rest.intake.model.request.PegaParamRequest;
import com.ing.bankguarantees.remote.rest.intake.model.request.PegaParamRequest.DecisionRequest;
import com.ing.bankguarantees.remote.rest.intake.model.request.PegaParamRequest.DocumentumIdentifierRequest;
import com.ing.bankguarantees.remote.rest.intake.model.request.PegaParamRequest.ExtraInfoRequest;
import com.ing.bankguarantees.service.referencedata.CurrencyDetailService;
import com.ing.bankguarantees.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.ing.bankguarantees.models.enums.StpCriteriaType.CREDIT_LINE_BALANCE;
import static com.ing.bankguarantees.models.enums.StpCriteriaType.SDS_RESPONSE;
import static com.ing.bankguarantees.utils.CommonUtils.getAmount;
import static com.ing.bankguarantees.utils.CommonUtils.getIdentifierValue;
import static com.ing.bankguarantees.utils.ConstantUtils.GRID_TYPE;
import static com.ing.bankguarantees.utils.JsonUtils.convert;


@Slf4j
@Component
@RequiredArgsConstructor
public class IntakeApiRequestMapper {

    private static final String PERF = "PERF";
    private static final String RENT = "RENT";
    private static final String APAY = "APAY";
    private static final String PAYM = "PAYM";
    private static final String RETE = "RETE";
    private static final String BIDB = "BIDB";
    private static final String CUST = "CUST";
    private static final String DECL = "DECL";
    private static final String OK = "OK";
    private static final String NOK = "NOK";
    private static final String YES = "Yes";
    private static final String NO = "No";
    private static final String YES_SHORT = "Y";
    private static final String NO_SHORT = "N";
    private static final String APPLICANT_DELIVERY_TYPE = "applicant";
    private static final String BENEFICIARY_DELIVERY_TYPE = "beneficiary";
    private static final String REQUEST_DOC = "REQUEST_DOC";
    private static final String FINAL_DOC = "FINAL_DOC";
    private static final String CREDIT_DOC = "CREDIT_DOC";
    private static final String CREDIT_FINAL = "CREDIT_FINAL";
    private static final String CUSTOMIZED_DOC = "CUSTOMIZED_DOC";
    private static final String GOODS_TRANSPORT_MODEL = "GOODS_TRANSPORTS";
    private static final LocalDate APPROXIMATE_EXPIRY_DATE = LocalDate.of(2074, 12, 31);


    private final IntakeApiProperties intakeApiProperties;
    private final CurrencyDetailService currencyService;

    @Value("${bgos.default-country-code}")
    private String countryCode;


    public IntakeApiRequest prepareIntakeApiRequest(IntakeApiInput intakeApiInput) {

        return IntakeApiRequest.builder()
                .parameters(preparePegaParamRequest(intakeApiInput))
                .applicationRequest(prepareApplicationRequest(intakeApiInput))
                .build();
    }


    private PegaParamRequest preparePegaParamRequest(IntakeApiInput intakeApiInput) {
        BankGuaranteeRequestData bankGuaranteeRequestData = intakeApiInput.bankGuaranteeRequest().getBgRequest();
        return PegaParamRequest.builder()
                .team(intakeApiProperties.getTeam())
                .indirectGuarantee(intakeApiProperties.getIndirectGuarantee())
                .orfAgreementReceived(intakeApiProperties.getOrfAgreementReceived())
                .otherDocumentsNeeded(intakeApiProperties.getOtherDocumentsNeeded())
                .decisionSheetReceived(LocalDate.now().minusDays(10))
                .customerSegmentation(intakeApiProperties.getCustomerSegment())
                .existingCreditLine(isExistingCreditLine(bankGuaranteeRequestData.getFinancialInformation()))
                .decisionSheetNeeded(isDecisionSheetNeeded(bankGuaranteeRequestData.getStpResultDataSet()).orElse(NO))
                .documentumIdentifiers(DocumentumIdentifierRequest.builder()
                        .requestDossierId(bankGuaranteeRequestData.getDossierInformation().getDossierResponseId())
                        .agreementDossierId(bankGuaranteeRequestData.getDossierInformation().getAgreementDossierResponseId())
                        .build())
                .extraInfo(ExtraInfoRequest.builder()
                        .decision(DecisionRequest.builder()
                                .creditLineResult(getCreditLineResult(bankGuaranteeRequestData.getStpResultDataSet()))
                                .pdlResult(getPdlResult(bankGuaranteeRequestData.getStpResultDataSet()))
                                .build())
                        .build())
                .build();
    }


    private ApplicationRequest prepareApplicationRequest(IntakeApiInput intakeApiInput) {
        BankGuaranteeRequest bankGuaranteeRequest = intakeApiInput.bankGuaranteeRequest();
        BankGuaranteeRequestData bankGuaranteeRequestData = bankGuaranteeRequest.getBgRequest();
        return ApplicationRequest.builder()
                .team(intakeApiProperties.getTiTeam())
                .isStp(bankGuaranteeRequestData.isStp() ? YES_SHORT : NO_SHORT)
                .isoIndicName(getIsoIndicName(bankGuaranteeRequestData.getStpResultDataSet()).orElse(NO.toUpperCase()))
                .garCover(intakeApiProperties.getGarCover())
                .masterReferenceId(bankGuaranteeRequest.getMasterReferenceNumber())
                .undertakingApplications(prepareUndertakingApplications(bankGuaranteeRequestData))
                .documents(getAllDocumentList(intakeApiInput.documents()))
                .applicant(getApplicant(bankGuaranteeRequestData))
                .obligor(getObligor(bankGuaranteeRequestData).orElse(null))
                .beneficiary(getBeneficiary(bankGuaranteeRequestData).orElse(getBlankPromiseBeneficiary(bankGuaranteeRequestData)))
                .deliveryChannel(getDeliveryChannel(bankGuaranteeRequestData.getDeliveryInformation()))
                .advisingPartyIndicator(NO)
                .counterUndertakingIndicator(NO)
                .build();
    }

    private Optional<BeneficiaryRequest> getBeneficiary(BankGuaranteeRequestData bankGuaranteeRequestData) {
        BankGuaranteeCode bgCode = bankGuaranteeRequestData.getGuaranteeDetails().getBgCode();
        BeneficiaryData beneficiary = bankGuaranteeRequestData.getBeneficiary();

        if (bgCode == BankGuaranteeCode.WOODS_PROM_B)
            return Optional.empty();

        String beneficiaryName = CommonUtils.getBeneficiaryName(beneficiary);
        return Optional.of(BeneficiaryRequest.builder()
                .name(beneficiaryName)
                .contactDetails(ContactDetailsRequest.builder()
                        .name(beneficiaryName)
                        .emailAddress(beneficiary.getEmailAddress().getEmailIdInformation())
                        .build())
                .postalAddress(PostalAddressRequest.builder()
                        .addressLine(beneficiary.getPostalAddress().getFirstAddress())
                        .townName(beneficiary.getPostalAddress().getCityName())
                        .country(beneficiary.getPostalAddress().getCountryCode())
                        .postCode(beneficiary.getPostalAddress().getPostalCode())
                        .build())
                .build());

    }


    private ApplicantRequest getApplicant(BankGuaranteeRequestData bankGuaranteeRequestData) {
        IndividualData individual = bankGuaranteeRequestData.getInstructingParty().getIndividual();
        OrganisationData organisation = bankGuaranteeRequestData.getInstructingParty().getOrganisation();
        ApplicantData applicant = bankGuaranteeRequestData.getApplicant();
        boolean issueToAnotherParty = bankGuaranteeRequestData.isIssueToAnotherParty();
        return ApplicantRequest.builder()
                .name(issueToAnotherParty ? applicant.getOrganisationName().getFullName() : organisation.getOrganisationName().getFullName())
                .postalAddress(PostalAddressRequest.builder()
                        .addressLine(issueToAnotherParty ? applicant.getPostalAddress().getFirstAddress() : organisation.getPostalAddress().getFirstAddress())
                        .country(issueToAnotherParty ? applicant.getPostalAddress().getCountryCode() : organisation.getPostalAddress().getCountryCode())
                        .townName(issueToAnotherParty ? applicant.getPostalAddress().getCityName() : organisation.getPostalAddress().getCityName())
                        .postCode(issueToAnotherParty ? applicant.getPostalAddress().getPostalCode() : organisation.getPostalAddress().getPostalCode())
                        .build())
                .contactDetails(ContactDetailsRequest.builder()
                        .name(individual.getIndividualName().getFullName())
                        .emailAddress(CommonUtils.getEmailDigitalAddress(individual.getDigitalAddresses()))
                        .phoneNumber(CommonUtils.getPhoneDigitalAddress(individual.getDigitalAddresses()))
                        .build())
                .organisation(!issueToAnotherParty ? OrganisationRequest.builder()
                        .idType(GRID_TYPE.toLowerCase())
                        .name(organisation.getOrganisationName().getFullName())
                        .id(getGridId(organisation.getInternalIdentifiers()))
                        .build() : null)
                .build();
    }

    private Optional<ObligorRequest> getObligor(BankGuaranteeRequestData bankGuaranteeRequestData) {
        IndividualData individual = bankGuaranteeRequestData.getInstructingParty().getIndividual();
        OrganisationData organisation = bankGuaranteeRequestData.getInstructingParty().getOrganisation();
        return bankGuaranteeRequestData.isIssueToAnotherParty() ? Optional.of(ObligorRequest.builder()
                .name(organisation.getOrganisationName().getFullName())
                .postalAddress(PostalAddressRequest.builder()
                        .addressLine(organisation.getPostalAddress().getFirstAddress())
                        .country(organisation.getPostalAddress().getCountryCode())
                        .townName(organisation.getPostalAddress().getCityName())
                        .postCode(organisation.getPostalAddress().getPostalCode())
                        .build())
                .contactDetails(ContactDetailsRequest.builder()
                        .name(individual.getIndividualName().getFullName())
                        .emailAddress(CommonUtils.getEmailDigitalAddress(individual.getDigitalAddresses()))
                        .phoneNumber(CommonUtils.getPhoneDigitalAddress(individual.getDigitalAddresses()))
                        .build())
                .organisation(OrganisationRequest.builder()
                        .idType(GRID_TYPE.toLowerCase())
                        .name(organisation.getOrganisationName().getFullName())
                        .id(getGridId(organisation.getInternalIdentifiers()))
                        .build())
                .build())
                : Optional.empty();
    }

    private UndertakingApplicationsDetailsRequest prepareUndertakingApplications(BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        FinancialInformationData financialInformation = bankGuaranteeRequestData.getFinancialInformation();
        BankGuaranteeCode bgCode = guaranteeDetails.getBgCode();
        return UndertakingApplicationsDetailsRequest.builder()
                .purpose(getPurpose(guaranteeDetails))
                .termsAndConditions(getPurpose(guaranteeDetails))
                .applicantReferenceNumber(bankGuaranteeRequestData.getReferenceNumber())
                .type(UndertakingTypeRequest.builder()
                        .code(getUndertakingTypeCode(bgCode))
                        .build())
                .undertakingAmount(UndertakingAmountRequest.builder()
                        .amount(getAmount(guaranteeDetails))
                        .currency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .build())
                .obligorChargeAccount(ObligorChargeAccountRequest.builder()
                        .currency(currencyService.getCurrencyCodeByValue(financialInformation.getAccountToBeDebited().getAccountCurrency()))
                        .identification(ObligorIdentificationRequest.builder()
                                .iban(financialInformation.getAccountToBeDebited().getIbanNumber())
                                .build())
                        .build())
                .obligorLiabilityAccount(ObligorLiabilityAccountRequest.builder()
                        .identification(ObligorIdentificationRequest.builder()
                                .iban(getCreditLineAccountNumber(financialInformation))
                                .build())
                        .currency(financialInformation.getCreditLine() == null
                                ? currencyService.getCurrencyCodeByValue(financialInformation.getAccountToBeDebited().getAccountCurrency())
                                : currencyService.getCurrencyCodeByValue(financialInformation.getCreditLine().getCurrency()))
                        .build())
                .undertakingWording(UndertakingWordingRequest.builder()
                        .modelForm(getModelForm(bgCode))
                        .requestedWordingLanguage(WordingLanguageRequest.builder()
                                .code(guaranteeDetails.getBgLanguage().getLanguageCode())
                                .build())
                        .build())
                .underlyingTransaction(UnderlyingTransactionRequest.builder()
                        .transactionDate(getContractDate(guaranteeDetails).orElse(null))
                        .identification(getIdentification(guaranteeDetails).orElse(null))
                        .transactionAmount(UndertakingAmountRequest.builder()
                                .amount(getAmount(guaranteeDetails))
                                .currency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                                .build())
                        .build())
                .expiryDetails(getExpiryDetails(bankGuaranteeRequestData))
                .build();
    }

    private String getModelForm(BankGuaranteeCode bgCode) {
        return bgCode == BankGuaranteeCode.GOODS_TRANSPORT ? GOODS_TRANSPORT_MODEL : bgCode.name();
    }

    private ExpiryDetailsRequest getExpiryDetails(BankGuaranteeRequestData bankGuaranteeRequestData) {
        Optional<LocalDate> optionalExpDate = getExpiryDate(bankGuaranteeRequestData);
        return ExpiryDetailsRequest.builder()
                .expiryTerms(ExpiryTermsRequest.builder()
                        .openEndedIndicator(optionalExpDate.isPresent() ? NO : YES)
                        .date(optionalExpDate.orElse(APPROXIMATE_EXPIRY_DATE))
                        .build())
                .build();
    }


    private String getPurpose(GuaranteeDetailsData<?> guaranteeDetails) {
        return switch (guaranteeDetails.getBgCode()) {
            case CUSTOM_1, CUSTOM_2, CUSTOM_4, CUSTOM_5 -> "Customs";
            case REAL_ESTATE -> "Real Estate";
            case STATE_LOTTERY -> "State Lottery";
            case RENTAL -> "Rental";
            case WOODS_PROM_A -> "Woods Wallonia Standard Promise";
            case WOODS_PROM_VLA -> "Woods Flanders Standard Promise";
            case WOODS_BGVLA_PUBLIC -> "Woods Flanders Bank Guarantee";
            case PERFORMANCE_BOND ->
                    ((PerformanceBond) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getContractDescription();
            case PUBLIC_CONTRACT ->
                    ((PublicContract) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getTitle();
            case OVAM ->
                    ((Ovam) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getReferenceNumber();
            case PAYMENT_GUARANTEE ->
                    ((Payment) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getContractDescription();
            case ADVANCE_PAYMENT ->
                    ((AdvancePayment) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getContractDescription();
            case MONEY_RETENTION_BOND ->
                    ((MoneyRetentionBond) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getContractDescription();
            case BID_BOND ->
                    ((BidBond) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getContractDescription();
            case DCK_CDC ->
                    ((Dck) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getContractDescription();
            case WOODS_PROM_B ->
                    ((WoodsBlankPromise) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getLotsDescription();
            case WOODS_BG_DISCHARGE ->
                    ((WoodsDischarge) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getLotsDescription();
            case WOODS_BGWAL_PUBLIC ->
                    ((WoodsWALPublic) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getLotsDescription();
            case WOODS_BG_PRIVATE ->
                    ((WoodsPrivate) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getLotsDescription();
            case GOODS_TRANSPORT -> "Goods Transport";
            case PASSENGER_TRANSPORT -> "Passenger Transport";
            case OPERATORS_TRANSPORT -> "Transport Operator";
            case ABSTRACT_PROM ->
                    ((PromiseAbstract) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getContractDescription();
            case PUBLIC_CONTRACT_PROM ->
                    ((PromisePublicContract) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getTitle();
            case CUSTOMIZED_TEXT ->
                    ((CustomizedText) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getContractDescription();
        };
    }

    private String getUndertakingTypeCode(BankGuaranteeCode code) {

        return switch (code) {
            case PERFORMANCE_BOND, PUBLIC_CONTRACT, OVAM, DCK_CDC, WOODS_BG_PRIVATE, WOODS_BGWAL_PUBLIC,
                 WOODS_BG_DISCHARGE, WOODS_BGVLA_PUBLIC, GOODS_TRANSPORT, PASSENGER_TRANSPORT,
                 OPERATORS_TRANSPORT, CUSTOMIZED_TEXT -> PERF;
            case CUSTOM_1, CUSTOM_2, CUSTOM_4, CUSTOM_5 -> CUST;
            case PAYMENT_GUARANTEE, REAL_ESTATE, STATE_LOTTERY -> PAYM;
            case ADVANCE_PAYMENT -> APAY;
            case MONEY_RETENTION_BOND -> RETE;
            case BID_BOND -> BIDB;
            case RENTAL -> RENT;
            case WOODS_PROM_A, WOODS_PROM_B, WOODS_PROM_VLA, ABSTRACT_PROM, PUBLIC_CONTRACT_PROM -> DECL;
        };

    }


    private List<DocumentsRequest> getAllDocumentList(List<Document> documents) {

        List<DocumentsRequest> documentsList = new ArrayList<>();
        for (Document document : documents) {
            documentsList.add(DocumentsRequest.builder()
                    .id(document.getDocumentId())
                    .type(getDocumentType(document.getDocumentType()))
                    .build());
        }
        return documentsList;
    }

    private String getDocumentType(DocumentType documentType) {

        return switch (documentType) {
            case BG_DRAFT -> REQUEST_DOC;
            case CONTRACT -> CREDIT_DOC;
            case BG_FINAL -> FINAL_DOC;
            case CONTRACT_FINAL -> CREDIT_FINAL;
            case CUSTOMIZED_DOC -> CUSTOMIZED_DOC;
        };
    }


    private Optional<String> getIsoIndicName(StpResultDataSet stpResult) {

        boolean sdsResponseStatus = stpResult.getStpResultByType(SDS_RESPONSE).map(StpResultDataSet.STPResultData::isStpPossible).orElse(false);
        return stpResult.getStpResultByType(CREDIT_LINE_BALANCE).flatMap(creditLineStatus ->
                (CreditLineStatus.valueOf(creditLineStatus.getJustification()) == CreditLineStatus.NOT_AVAILABLE) && sdsResponseStatus
                        ? Optional.of(YES.toUpperCase())
                        : Optional.of(NO.toUpperCase())
        );
    }

    private Optional<String> isDecisionSheetNeeded(StpResultDataSet stpResult) {
        boolean sdsResponseStatus = stpResult.getStpResultByType(SDS_RESPONSE).map(StpResultDataSet.STPResultData::isStpPossible).orElse(false);
        return stpResult.getStpResultByType(CREDIT_LINE_BALANCE).flatMap(creditLineStatus ->
                (CreditLineStatus.valueOf(creditLineStatus.getJustification()) == CreditLineStatus.SUFFICIENT_BALANCE) || sdsResponseStatus
                        ? Optional.of(NO)
                        : Optional.of(YES)
        );
    }

    public String getCreditLineAccountNumber(FinancialInformationData financialInformationData) {
        return Optional.ofNullable(financialInformationData.getCreditLine())
                .map(FinancialInformationData.ContractDetailData::getIbanNumber)
                .orElse(financialInformationData.getAccountToBeDebited().getIbanNumber());
    }


    private Optional<LocalDate> getContractDate(GuaranteeDetailsData<?> guaranteeDetails) {
        return switch (guaranteeDetails.getBgCode()) {
            case STATE_LOTTERY ->
                    Optional.of(((StateLottery) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getDateOfAgreeInPrinc());
            case RENTAL ->
                    Optional.of(((Rental) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getDateOfSignature());
            case PUBLIC_CONTRACT ->
                    Optional.of(((PublicContract) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getGrantDate());
            case OVAM ->
                    Optional.of(((Ovam) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getWasteTransportStartDate());
            case WOODS_PROM_A ->
                    Optional.of(((WoodsWalloniaStandardPromise) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getSaleDate());
            case WOODS_PROM_VLA ->
                    Optional.of(((WoodsFlandersStandardPromise) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getSaleDate());
            case WOODS_BG_DISCHARGE ->
                    Optional.of(((WoodsDischarge) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getSaleDate());
            case WOODS_BG_PRIVATE ->
                    Optional.of(((WoodsPrivate) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getSaleDate());
            case WOODS_BGVLA_PUBLIC ->
                    Optional.of(((WoodsVLAPublic) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getSaleDate());
            case WOODS_BGWAL_PUBLIC ->
                    Optional.of(((WoodsWALPublic) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getSaleDate());
            default -> Optional.empty();
        };
    }


    private String isExistingCreditLine(FinancialInformationData financialInformationData) {
        return ObjectUtils.isEmpty(financialInformationData.getCreditLine())
                ? NO
                : YES;
    }

    private String getPdlResult(StpResultDataSet stpResult) {
        return stpResult.getStpResultByType(SDS_RESPONSE).map(StpResultDataSet.STPResultData::isStpPossible).orElse(false)
                ? OK
                : NOK;
    }

    private String getCreditLineResult(StpResultDataSet stpResult) {
        return stpResult.getStpResultByType(CREDIT_LINE_BALANCE).map(StpResultDataSet.STPResultData::isStpPossible).orElse(false)
                ? OK
                : NOK;
    }

    private DeliveryChannelRequest getDeliveryChannel(DeliveryInformationData deliveryInformation) {
        return DeliveryChannelRequest.builder()
                .deliverToPartyType(deliveryInformation.getRecipient() == BankGuaranteeRecipient.ME
                        ? APPLICANT_DELIVERY_TYPE
                        : BENEFICIARY_DELIVERY_TYPE)
                .build();
    }

    private String getGridId(List<Identifier> internalIdentifiers) {
        return getIdentifierValue(internalIdentifiers, List.of(GRID_TYPE))
                .map(Identifier::getValue)
                .orElse(null);
    }

    private Optional<LocalDate> getExpiryDate(BankGuaranteeRequestData bankGuaranteeRequestData) {

        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        return switch (bankGuaranteeRequestData.getGuaranteeDetails().getBgCode()) {

            case PERFORMANCE_BOND -> getPerformanceBondExpiryDate(bankGuaranteeRequestData);
            case ADVANCE_PAYMENT -> getAdvancePaymentExpiryDate(bankGuaranteeRequestData);
            case MONEY_RETENTION_BOND -> getMoneyRetentionExpiryDate(bankGuaranteeRequestData);
            case PAYMENT_GUARANTEE -> getPaymentExpiryDate(bankGuaranteeRequestData);
            case BID_BOND -> getBidBondExpiryDate(bankGuaranteeRequestData);
            case CUSTOMIZED_TEXT -> getCustomizedTextExpiryDate(bankGuaranteeRequestData);
            case RENTAL ->
                    Optional.of(((Rental) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getRentalEndDate());
            case OVAM ->
                    Optional.of(((Ovam) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getContractValidityEndDate());
            case WOODS_PROM_A ->
                    Optional.of(((WoodsWalloniaStandardPromise) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getPromiseEndDate());
            case WOODS_PROM_B ->
                    Optional.of(((WoodsBlankPromise) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getPromiseEndDate());
            case WOODS_BG_PRIVATE ->
                    Optional.of(((WoodsPrivate) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getMaturityDate());
            case ABSTRACT_PROM ->
                    Optional.of(((PromiseAbstract) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getPromiseEndDate());
            case PUBLIC_CONTRACT_PROM ->
                    Optional.of(((PromisePublicContract) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getPromiseEndDate());
            default -> Optional.empty();
        };
    }

    private Optional<LocalDate> getPerformanceBondExpiryDate(BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        PerformanceBond performanceBond = ((PerformanceBond) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType()));
        return performanceBond.getBankGuaranteeEndType() == BankGuaranteeEndType.SPECIFIED
                ? Optional.of(performanceBond.getMaturityDate())
                : performanceBond.getBankGuaranteeEndType() == BankGuaranteeEndType.ACCEPTANCE_WITH_EXPIRY_DATE
                ? Optional.of(performanceBond.getFinalMaturityDate())
                : Optional.empty();
    }

    private Optional<LocalDate> getAdvancePaymentExpiryDate(BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        AdvancePayment advancePayment = ((AdvancePayment) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType()));
        return advancePayment.getBankGuaranteeEndType() == BankGuaranteeEndType.SPECIFIED
                ? Optional.of(advancePayment.getMaturityDate())
                : Optional.empty();
    }

    private Optional<LocalDate> getMoneyRetentionExpiryDate(BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        MoneyRetentionBond moneyRetentionBond = ((MoneyRetentionBond) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType()));
        return moneyRetentionBond.getBankGuaranteeEndType() == BankGuaranteeEndType.SPECIFIED
                ? Optional.of(moneyRetentionBond.getMaturityDate())
                : Optional.empty();
    }

    private Optional<LocalDate> getPaymentExpiryDate(BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Payment payment = ((Payment) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType()));
        return payment.getBankGuaranteeEndType() == BankGuaranteeEndType.SPECIFIED
                ? Optional.of(payment.getMaturityDate())
                : Optional.empty();
    }


    private Optional<LocalDate> getBidBondExpiryDate(BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        BidBond bidBond = ((BidBond) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType()));
        return bidBond.getBankGuaranteeEndType() == BankGuaranteeEndType.SPECIFIED
                ? Optional.of(bidBond.getMaturityDate())
                : Optional.empty();
    }


    private Optional<LocalDate> getCustomizedTextExpiryDate(BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        CustomizedText customizedText = ((CustomizedText) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType()));
        return customizedText.getBankGuaranteeEndType() == BankGuaranteeEndType.SPECIFIED
                ? Optional.of(customizedText.getMaturityDate())
                : Optional.empty();
    }


    private BeneficiaryRequest getBlankPromiseBeneficiary(BankGuaranteeRequestData bankGuaranteeRequestData) {
        OrganisationData organisation = bankGuaranteeRequestData.getInstructingParty().getOrganisation();
        return BeneficiaryRequest.builder()
                .name("N.A")
                .contactDetails(ContactDetailsRequest.builder()
                        .name("N.A")
                        .emailAddress(CommonUtils.getEmailDigitalAddress(organisation.getDigitalAddresses()))
                        .build())
                .postalAddress(PostalAddressRequest.builder()
                        .addressLine("N.A")
                        .townName("N.A")
                        .country(countryCode)
                        .postCode("1234")
                        .build())
                .build();
    }


    private Optional<String> getIdentification(GuaranteeDetailsData<?> guaranteeDetails) {
        return switch (guaranteeDetails.getBgCode()) {
            case PUBLIC_CONTRACT ->
                    Optional.of(((PublicContract) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getReferenceNumber());
            case PERFORMANCE_BOND ->
                    Optional.of(((PerformanceBond) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getReferenceNumber());
            case RENTAL -> getRentalRefNumber(guaranteeDetails);
            case ADVANCE_PAYMENT ->
                    Optional.of(((AdvancePayment) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getReferenceNumber());
            case PAYMENT_GUARANTEE ->
                    Optional.of(((Payment) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getReferenceNumber());
            case MONEY_RETENTION_BOND ->
                    Optional.of(((MoneyRetentionBond) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getReferenceNumber());
            case BID_BOND ->
                    Optional.of(((BidBond) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getReferenceNumber());
            case OVAM ->
                    Optional.of(((Ovam) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getReferenceNumber());
            case CUSTOMIZED_TEXT ->
                    Optional.of(((CustomizedText) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getReferenceNumber());
            default -> Optional.empty();

        };
    }

    private Optional<String> getRentalRefNumber(GuaranteeDetailsData<?> guaranteeDetails) {
        String refNumber = ((Rental) convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType())).getReferenceNumber();
        return refNumber == null ? Optional.empty() : Optional.of(refNumber);
    }
}
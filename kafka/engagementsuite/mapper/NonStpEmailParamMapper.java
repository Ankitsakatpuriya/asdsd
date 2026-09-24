package com.ing.bankguarantees.remote.kafka.engagementsuite.mapper;

import com.ing.bankguarantees.models.domain.*;
import com.ing.bankguarantees.models.domain.FinancialInformationData.AccountData;
import com.ing.bankguarantees.models.domain.FinancialInformationData.ContractDetailData;
import com.ing.bankguarantees.models.enums.*;
import com.ing.bankguarantees.models.guaranteetype.*;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.EmailNotificationInput;
import com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationHelper;
import com.ing.bankguarantees.service.referencedata.CountryDetailService;
import com.ing.bankguarantees.service.referencedata.CurrencyDetailService;
import com.ing.bankguarantees.utils.CommonUtils;
import com.ing.bankguarantees.utils.ConstantUtils;
import com.ing.bankguarantees.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.ing.bankguarantees.models.enums.BankGuaranteeEndType.ACCEPTANCE_WITH_EXPIRY_DATE;
import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationEventParams.*;
import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationHelper.*;
import static com.ing.bankguarantees.utils.CommonUtils.*;
import static com.ing.bankguarantees.utils.ConstantUtils.DATE_TIME_FORMATTER_DD_MM_YYYY;

@Component
@RequiredArgsConstructor
public class NonStpEmailParamMapper {

    @Value("${bgos.dossier-url}")
    private String dossierUrl;

    private final CurrencyDetailService currencyDetailService;
    private final CountryDetailService countryDetailService;

    private static final String AGREEMENT_DOSSIER_TYPE_CODE = "BLEBGAD01";
    private static final String DOC_ID_TEXT = "docid";


    public Map<String, String> prepareBodyParam(EmailNotificationInput emailNotificationInput) {
        Map<String, String> templateBodyParams = new HashMap<>();
        BankGuaranteeRequestData bankGuaranteeRequestData = emailNotificationInput.getBankGuaranteeRequest().getBgRequest();
        prepareGeneral(templateBodyParams, bankGuaranteeRequestData);
        prepareInstructingParty(templateBodyParams, bankGuaranteeRequestData);
        prepareLegalRepInformation(templateBodyParams, bankGuaranteeRequestData);
        prepareApplicant(templateBodyParams, bankGuaranteeRequestData);
        prepareBankGuaranteeInformation(templateBodyParams, bankGuaranteeRequestData);
        prepareSendTo(templateBodyParams, bankGuaranteeRequestData);
        prepareAccountDetail(templateBodyParams, bankGuaranteeRequestData);
        prepareBeneficiaryBodyParameter(templateBodyParams, bankGuaranteeRequestData);
        prepareStpResultSet(templateBodyParams, emailNotificationInput.getBankGuaranteeRequest());
        prepareDocumentDetails(templateBodyParams, bankGuaranteeRequestData, emailNotificationInput.getDocumentList());
        return NotificationHelper.sanitizeParams(templateBodyParams);
    }

    public Map<String, String> prepareAttachmentParameter(EmailNotificationInput emailNotificationInput) {
        BankGuaranteeRequestData bankGuaranteeRequestData = emailNotificationInput.getBankGuaranteeRequest().getBgRequest();
        Map<String, String> attachmentParams = new HashMap<>();
        prepareGeneral(attachmentParams, bankGuaranteeRequestData);
        prepareLegalRepInformation(attachmentParams, bankGuaranteeRequestData);
        prepareApplicant(attachmentParams, bankGuaranteeRequestData);
        prepareBankGuaranteeInformation(attachmentParams, bankGuaranteeRequestData);
        prepareSendTo(attachmentParams, bankGuaranteeRequestData);
        prepareAccountDetail(attachmentParams, bankGuaranteeRequestData);
        prepareBeneficiaryAttachParameter(attachmentParams, bankGuaranteeRequestData);
        prepareStpResultSet(attachmentParams, emailNotificationInput.getBankGuaranteeRequest());
        return NotificationHelper.sanitizeParams(attachmentParams);
    }


    private void prepareGeneral(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        attachmentParams.put(CREATION_DATE, formatDateTime(LocalDateTime.now(), DATE_TIME_FORMATTER_YYYY_MM_DD_HH));
        attachmentParams.put(UPDATE_DATE, formatDateTime(LocalDateTime.now(), DATE_TIME_FORMATTER_YYYY_MM_DD_HH));
        attachmentParams.put(ISSUE_FOR_ANOTHER_PARTY, bankGuaranteeRequestData.isIssueToAnotherParty() ? "0" : "1");
        attachmentParams.put(REMARKS, checkCreditLineNotAvailableAndSdsIsPossible(bankGuaranteeRequestData.getStpResultDataSet()) ? "BCC" : StringUtils.EMPTY);
    }

    private void prepareLegalRepInformation(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        InstructingPartyData.IndividualData individualData = bankGuaranteeRequestData.getInstructingParty().getIndividual();
        attachmentParams.put(LEGAL_REP_NAME, individualData.getIndividualName().getFullName());
        attachmentParams.put(LEGAL_REP_EMAIL, getLegalRepresentativeDigitalAddress(individualData, ConstantUtils.DIGITAL_TYPE_EMAIL).orElse(StringUtils.EMPTY));
        attachmentParams.put(LEGAL_REP_PHONE, getLegalRepresentativeDigitalAddress(individualData, ConstantUtils.DIGITAL_TYPE_TEL).orElse(StringUtils.EMPTY));
    }

    private void prepareInstructingParty(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        InstructingPartyData.OrganisationData organisation = bankGuaranteeRequestData.getInstructingParty().getOrganisation();
        attachmentParams.put(INSTRUCTING_PARTY_KBO, CommonUtils.formatKboNumber(organisation.getCinNumber()));
        attachmentParams.put(INSTRUCTING_PARTY_NAME, organisation.getOrganisationName().getFullName());
        attachmentParams.put(INSTRUCTING_PARTY_STREET, organisation.getPostalAddress().getFirstAddress());
        attachmentParams.put(INSTRUCTING_PARTY_CITY, organisation.getPostalAddress().getCityName());
        attachmentParams.put(INSTRUCTING_PARTY_COUNTRY, getCountryNameByCodeAndLocale(organisation.getPostalAddress().getCountryCode()));
        attachmentParams.put(INSTRUCTING_PARTY_ZIP, organisation.getPostalAddress().getPostalCode());
        attachmentParams.put(INSTRUCTING_PARTY_ID, organisation.getLegalEntityId());
    }

    private void prepareApplicant(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        InstructingPartyData.OrganisationData organisation = bankGuaranteeRequestData.getInstructingParty().getOrganisation();
        ApplicantData applicant = bankGuaranteeRequestData.getApplicant();
        boolean issueToAnotherParty = bankGuaranteeRequestData.isIssueToAnotherParty();
        attachmentParams.put(APPLICANT_KBO, CommonUtils.formatKboNumber(organisation.getCinNumber()));
        attachmentParams.put(APPLICANT_NAME, issueToAnotherParty ? applicant.getOrganisationName().getFullName() : organisation.getOrganisationName().getFullName());
        attachmentParams.put(APPLICANT_STREET, issueToAnotherParty ? applicant.getPostalAddress().getFirstAddress() : organisation.getPostalAddress().getFirstAddress());
        attachmentParams.put(APPLICANT_CITY, issueToAnotherParty ? applicant.getPostalAddress().getCityName() : organisation.getPostalAddress().getCityName());
        attachmentParams.put(APPLICANT_COUNTRY, issueToAnotherParty ? getCountryNameByCodeAndLocale(applicant.getPostalAddress().getCountryCode()) : getCountryNameByCodeAndLocale(organisation.getPostalAddress().getCountryCode()));
        attachmentParams.put(APPLICANT_ZIP, issueToAnotherParty ? applicant.getPostalAddress().getPostalCode() : organisation.getPostalAddress().getPostalCode());
        attachmentParams.put(INSTRUCTING_PARTY, issueToAnotherParty ? organisation.getOrganisationName().getFullName() : StringUtils.EMPTY);
    }

    private void prepareAccountDetail(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        FinancialInformationData financialInformationData = bankGuaranteeRequestData.getFinancialInformation();
        AccountData accountToBeDebited = financialInformationData.getAccountToBeDebited();
        ContractDetailData creditLine = financialInformationData.getCreditLine();
        String accountNumberToBeDebited = getAccountNumberToBeDebited(accountToBeDebited).orElse(StringUtils.EMPTY);
        String creditLineAccountNumber = getAccountNumberCreditLine(creditLine).orElse(accountNumberToBeDebited);
        attachmentParams.put(ACCOUNT_TO_DEBIT, accountNumberToBeDebited);
        attachmentParams.put(ACCOUNT_TO_DEBIT_NAME, accountToBeDebited.getAccountName());
        attachmentParams.put(ACCOUNT_TO_DEBIT_CURRENCY, currencyDetailService.getCurrencyCodeByValue(accountToBeDebited.getAccountCurrency()));
        attachmentParams.put(ACCOUNT_TO_DEBIT_AMOUNT, accountToBeDebited.getBalanceAmount().toPlainString());
        attachmentParams.put(INSTRUCTING_BANK_ACCOUNT, bankGuaranteeRequestData.isIssueToAnotherParty() ? accountNumberToBeDebited : StringUtils.EMPTY);
        attachmentParams.put(ACCOUNT_CREDIT_LINE_AVAILABLE_STATUS, creditLine != null ? "1" : "0");
        attachmentParams.put(ACCOUNT_CREDIT_LINE, creditLineAccountNumber);
        if (creditLine != null) {
            attachmentParams.put(ACCOUNT_CREDIT_LINE_NAME, creditLine.getAccountName());
            attachmentParams.put(ACCOUNT_CREDIT_LINE_CURRENCY, currencyDetailService.getCurrencyCodeByValue(creditLine.getCurrency()));
            attachmentParams.put(ACCOUNT_CREDIT_LINE_AMOUNT, getAccountBalanceCreditLine(creditLine).orElse(StringUtils.EMPTY));
        }
    }

    private void prepareBeneficiaryAttachParameter(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        BankGuaranteeCode bgCode = bankGuaranteeRequestData.getGuaranteeDetails().getBgCode();
        if (bgCode != BankGuaranteeCode.WOODS_PROM_B) {
            BeneficiaryData beneficiary = bankGuaranteeRequestData.getBeneficiary();

            if (beneficiary.getBeneficiaryType() == BeneficiaryType.PRIVATE_INDIVIDUAL) {
                preparePrivateIndividualBeneficiary(attachmentParams, beneficiary);
            } else if (beneficiary.getBeneficiaryType() == BeneficiaryType.COMPANY) {
                attachmentParams.put(BENEFICIARY_NAME, beneficiary.getOrganisationName().getFullName());
            }

            attachmentParams.put(BENEFICIARY_KBO, CommonUtils.formatKboNumber(beneficiary.getCinNumber()));
            attachmentParams.put(BENEFICIARY_EMAIL, beneficiary.getEmailAddress().getEmailIdInformation());
            attachmentParams.put(BENEFICIARY_STREET, beneficiary.getPostalAddress().getFirstAddress());
            attachmentParams.put(BENEFICIARY_COUNTRY, getCountryNameByCodeAndLocale(beneficiary.getPostalAddress().getCountryCode()));
            attachmentParams.put(BENEFICIARY_ZIP, beneficiary.getPostalAddress().getPostalCode());
            attachmentParams.put(BENEFICIARY_CITY, beneficiary.getPostalAddress().getCityName());
            attachmentParams.put(BENEFICIARY_TYPE, beneficiary.getBeneficiaryType().getValue());
        }
    }

    private void preparePrivateIndividualBeneficiary(Map<String, String> attachmentParams, BeneficiaryData beneficiary) {
        var secondaryBeneficiaryName = beneficiary.getPrivateIndividual().getSecondaryBeneficiaryName();
        var primaryBeneficiaryName = beneficiary.getPrivateIndividual().getPrimaryBeneficiaryName();
        var primaryIdentificationReference = beneficiary.getPrivateIndividual().getPrimaryIdentificationReference();
        var secondaryIdentificationReference = beneficiary.getPrivateIndividual().getSecondaryIdentificationReference();
        var primaryBeneficiaryDob = beneficiary.getPrivateIndividual().getPrimaryBeneficiaryDob();
        var secondaryBeneficiaryDob = beneficiary.getPrivateIndividual().getSecondaryBeneficiaryDob();

        attachmentParams.put(BENEFICIARY_NAME, StringUtils.isEmpty(secondaryBeneficiaryName)
                ? primaryBeneficiaryName
                : String.join(",", primaryBeneficiaryName, secondaryBeneficiaryName));
        if (beneficiary.getPrivateIndividual().isBelgiumCitizen()) {
            attachmentParams.put(IDENTIFICATION_REFERENCE, getBeneficiaryId(primaryIdentificationReference, secondaryIdentificationReference));
        } else {
            attachmentParams.put(IDENTIFICATION_REFERENCE, getBeneficiaryDob(primaryBeneficiaryDob, secondaryBeneficiaryDob));
        }
    }

    private String getBeneficiaryId(String primaryIdentificationReference, String secondaryIdentificationReference) {
        return StringUtils.isNotEmpty(primaryIdentificationReference) && StringUtils.isNotEmpty(secondaryIdentificationReference)
                ? String.join(",", CommonUtils.formatReferenceIdNumber(primaryIdentificationReference), CommonUtils.formatReferenceIdNumber(secondaryIdentificationReference))
                : primaryIdentificationReference;
    }

    private String getBeneficiaryDob(LocalDate primaryBeneficiaryDob, LocalDate secondaryBeneficiaryDob) {
        return primaryBeneficiaryDob != null && secondaryBeneficiaryDob != null
                ? String.join(",", formatDate(primaryBeneficiaryDob, DATE_TIME_FORMATTER_DD_MM_YYYY), formatDate(secondaryBeneficiaryDob, DATE_TIME_FORMATTER_DD_MM_YYYY))
                : formatDate(primaryBeneficiaryDob, DATE_TIME_FORMATTER_DD_MM_YYYY);
    }

    private void prepareBeneficiaryBodyParameter(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        BankGuaranteeCode bgCode = bankGuaranteeRequestData.getGuaranteeDetails().getBgCode();
        if (bgCode != BankGuaranteeCode.WOODS_PROM_B) {
            BeneficiaryData beneficiary = bankGuaranteeRequestData.getBeneficiary();
            if (beneficiary.getBeneficiaryType() == BeneficiaryType.PRIVATE_INDIVIDUAL) {
                prepareDckBeneficiary(attachmentParams, beneficiary);
            } else if (beneficiary.getBeneficiaryType() == BeneficiaryType.COMPANY) {
                attachmentParams.put(BENEFICIARY_NAME, beneficiary.getOrganisationName().getFullName());
            }
            attachmentParams.put(BENEFICIARY_KBO, CommonUtils.formatKboNumber(beneficiary.getCinNumber()));
            attachmentParams.put(BENEFICIARY_EMAIL, beneficiary.getEmailAddress().getEmailIdInformation());
            attachmentParams.put(BENEFICIARY_STREET, beneficiary.getPostalAddress().getFirstAddress());
            attachmentParams.put(BENEFICIARY_COUNTRY, getCountryNameByCodeAndLocale(beneficiary.getPostalAddress().getCountryCode()));
            attachmentParams.put(BENEFICIARY_ZIP, beneficiary.getPostalAddress().getPostalCode());
            attachmentParams.put(BENEFICIARY_CITY, beneficiary.getPostalAddress().getCityName());
            attachmentParams.put(BENEFICIARY_TYPE, beneficiary.getBeneficiaryType().name());
        }
    }

    private void prepareDckBeneficiary(Map<String, String> attachmentParams, BeneficiaryData beneficiary) {
        attachmentParams.put(PRIMARY_BENEFICIARY_NAME, beneficiary.getPrivateIndividual().getPrimaryBeneficiaryName());
        attachmentParams.put(SECONDARY_BENEFICIARY_NAME, StringUtils.isNotEmpty(beneficiary.getPrivateIndividual().getSecondaryBeneficiaryName())
                ? beneficiary.getPrivateIndividual().getSecondaryBeneficiaryName()
                : null);
        attachmentParams.put(PRIMARY_IDENTIFICATION_REFERENCE, StringUtils.isNotEmpty(beneficiary.getPrivateIndividual().getPrimaryIdentificationReference())
                ? formatReferenceIdNumber(beneficiary.getPrivateIndividual().getPrimaryIdentificationReference())
                : null);
        attachmentParams.put(SECONDARY_IDENTIFICATION_REFERENCE, StringUtils.isNotEmpty(beneficiary.getPrivateIndividual().getSecondaryIdentificationReference())
                ? formatReferenceIdNumber(beneficiary.getPrivateIndividual().getSecondaryIdentificationReference())
                : null);
        attachmentParams.put(PRIMARY_IDENTIFICATION_DOB, beneficiary.getPrivateIndividual().getPrimaryBeneficiaryDob() != null
                ? formatDate(beneficiary.getPrivateIndividual().getPrimaryBeneficiaryDob(), DATE_TIME_FORMATTER_DD_MM_YYYY)
                : null);
        attachmentParams.put(SECONDARY_IDENTIFICATION_DOB, beneficiary.getPrivateIndividual().getSecondaryBeneficiaryDob() != null
                ? formatDate(beneficiary.getPrivateIndividual().getSecondaryBeneficiaryDob(), DATE_TIME_FORMATTER_DD_MM_YYYY)
                : null);
    }

    private String getCountryNameByCodeAndLocale(String countryCode) {
        return countryDetailService.getCountryNameByCode(Locale.ENGLISH, countryCode);
    }

    private void prepareSendTo(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {

        BankGuaranteeCode bgCode = bankGuaranteeRequestData.getGuaranteeDetails().getBgCode();
        BankGuaranteeRecipient sendGuaranteeTo = bankGuaranteeRequestData.getDeliveryInformation().getRecipient();
        ApplicantData applicant = bankGuaranteeRequestData.getApplicant();
        InstructingPartyData.OrganisationData organisation = bankGuaranteeRequestData.getInstructingParty().getOrganisation();
        BeneficiaryData beneficiary = bankGuaranteeRequestData.getBeneficiary();
        boolean issueToAnotherParty = bankGuaranteeRequestData.isIssueToAnotherParty();
        attachmentParams.put(SEND_TO, sendGuaranteeTo.name());
        attachmentParams.put(SEND_MODE, bankGuaranteeRequestData.getDeliveryInformation().getMode().getDescription());
        if (sendGuaranteeTo == BankGuaranteeRecipient.ME) {
            attachmentParams.put(SEND_TO_NAME, issueToAnotherParty ? applicant.getOrganisationName().getFullName() : organisation.getOrganisationName().getFullName());
            attachmentParams.put(SEND_TO_COMPANY, issueToAnotherParty ? applicant.getOrganisationName().getFullName() : organisation.getOrganisationName().getFullName());
            attachmentParams.put(SEND_TO_STREET, issueToAnotherParty ? applicant.getPostalAddress().getFirstAddress() : organisation.getPostalAddress().getFirstAddress());
            attachmentParams.put(SEND_TO_CITY, issueToAnotherParty ? applicant.getPostalAddress().getCityName() : organisation.getPostalAddress().getCityName());
            attachmentParams.put(SEND_TO_ZIP, issueToAnotherParty ? applicant.getPostalAddress().getPostalCode() : organisation.getPostalAddress().getPostalCode());
            attachmentParams.put(SEND_TO_MAIL, issueToAnotherParty ? applicant.getEmailAddress().getEmailIdInformation() : getEmailDigitalAddress(organisation.getDigitalAddresses()));
            attachmentParams.put(SEND_TO_COUNTRY, issueToAnotherParty ? getCountryNameByCodeAndLocale(applicant.getPostalAddress().getCountryCode()) : getCountryNameByCodeAndLocale(organisation.getPostalAddress().getCountryCode()));
        }
        if (sendGuaranteeTo == BankGuaranteeRecipient.BENEFICIARY && bgCode != BankGuaranteeCode.WOODS_PROM_B) {
            attachmentParams.put(SEND_TO_NAME, CommonUtils.getBeneficiaryName(beneficiary));
            attachmentParams.put(SEND_TO_COMPANY, CommonUtils.getBeneficiaryName(beneficiary));
            attachmentParams.put(SEND_TO_STREET, beneficiary.getPostalAddress().getFirstAddress());
            attachmentParams.put(SEND_TO_CITY, beneficiary.getPostalAddress().getCityName());
            attachmentParams.put(SEND_TO_ZIP, beneficiary.getPostalAddress().getPostalCode());
            attachmentParams.put(SEND_TO_COUNTRY, getCountryNameByCodeAndLocale(beneficiary.getPostalAddress().getCountryCode()));
        }
    }


    private void prepareBankGuaranteeInformation(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        attachmentParams.put(TYPE_NUMBER, guaranteeDetails.getBgCode().getTypeCode());
        attachmentParams.put(BG_CODE, guaranteeDetails.getBgCode().toString());
        attachmentParams.put(BG_LANG, guaranteeDetails.getBgLanguage().getLanguageCode().toUpperCase());
        attachmentParams.put(BG_LANG_FULL_NAME, guaranteeDetails.getBgLanguage().name());
        attachmentParams.put(TRANSLATION_LANGUAGE, bankGuaranteeRequestData.getTranslationLanguage().getLanguage().toUpperCase());
        attachmentParams.put(BG_CURRENCY, currencyDetailService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()));
        attachmentParams.put(BG_AMOUNT, guaranteeDetails.getBgAmount().toPlainString());
        attachmentParams.put(REFERENCE_NUMBER, bankGuaranteeRequestData.getReferenceNumber());
        switch (bankGuaranteeRequestData.getGuaranteeDetails().getBgCode()) {
            case RENTAL -> prepareRentalSpecificDetail(attachmentParams, bankGuaranteeRequestData);
            case STATE_LOTTERY -> prepareStateLotteryDetails(attachmentParams, bankGuaranteeRequestData);
            case PUBLIC_CONTRACT -> preparePublicContractDetails(attachmentParams, bankGuaranteeRequestData);
            case OVAM -> prepareOVAMDetails(attachmentParams, bankGuaranteeRequestData);
            case CUSTOM_1 -> prepareCustomOneDetails(attachmentParams, bankGuaranteeRequestData);
            case REAL_ESTATE -> prepareRealEstateDetails(attachmentParams, bankGuaranteeRequestData);
            case CUSTOM_2 -> prepareCustomTwoDetails(attachmentParams, bankGuaranteeRequestData);
            case CUSTOM_5 -> prepareCustomFiveDetails(attachmentParams, bankGuaranteeRequestData);
            case CUSTOM_4 -> prepareCustomFourDetails(attachmentParams, bankGuaranteeRequestData);
            case BID_BOND -> prepareBidBondDetails(attachmentParams, bankGuaranteeRequestData);
            case ADVANCE_PAYMENT -> prepareAdvancePaymentDetails(attachmentParams, bankGuaranteeRequestData);
            case PAYMENT_GUARANTEE -> preparePaymentGuaranteeDetails(attachmentParams, bankGuaranteeRequestData);
            case PERFORMANCE_BOND -> preparePerformanceBondDetails(attachmentParams, bankGuaranteeRequestData);
            case MONEY_RETENTION_BOND -> prepareMoneyRetentionDetails(attachmentParams, bankGuaranteeRequestData);
            case DCK_CDC -> prepareDckDetails(attachmentParams, bankGuaranteeRequestData);
            case WOODS_PROM_A -> prepareStandardPromiseDetails(attachmentParams, bankGuaranteeRequestData);
            case WOODS_PROM_VLA -> prepareFlandersStandardPromiseDetails(attachmentParams, bankGuaranteeRequestData);
            case WOODS_PROM_B -> prepareBlankPromise(attachmentParams, bankGuaranteeRequestData);
            case WOODS_BG_PRIVATE -> prepareWoodsPrivateSaleDetails(attachmentParams, bankGuaranteeRequestData);
            case WOODS_BG_DISCHARGE -> prepareWoodsDischargeDetails(attachmentParams, bankGuaranteeRequestData);
            case WOODS_BGWAL_PUBLIC -> prepareWoodsWALPublicDetails(attachmentParams, bankGuaranteeRequestData);
            case WOODS_BGVLA_PUBLIC -> prepareWoodsVLAPublicDetails(attachmentParams, bankGuaranteeRequestData);
            case GOODS_TRANSPORT -> prepareGoodsTransportDetails(attachmentParams, bankGuaranteeRequestData);
            case PASSENGER_TRANSPORT -> preparePassengerTransportDetails(attachmentParams, bankGuaranteeRequestData);
            case OPERATORS_TRANSPORT -> prepareOperatorTransportDetails(attachmentParams, bankGuaranteeRequestData);
            case ABSTRACT_PROM -> prepareAbstractPromiseDetails(attachmentParams, bankGuaranteeRequestData);
            case PUBLIC_CONTRACT_PROM ->
                    preparePublicContractPromiseDetails(attachmentParams, bankGuaranteeRequestData);
            case CUSTOMIZED_TEXT -> prepareCustomizedTextDetails(attachmentParams, bankGuaranteeRequestData);
        }
    }


    private void prepareMoneyRetentionDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        MoneyRetentionBond moneyRetentionBond = (MoneyRetentionBond) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(CONTRACT_REFERENCE, moneyRetentionBond.getReferenceNumber());
        attachmentParams.put(CONTRACT_DESCRIPTION, moneyRetentionBond.getContractDescription());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, moneyRetentionBond.getBankGuaranteeEnd());
        attachmentParams.put(BG_ACCOUNT, formatIbanNumber(moneyRetentionBond.getAdvancePaymentIBAN()));
        attachmentParams.put(BG_EXPIRY, moneyRetentionBond.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DT, formatDate(moneyRetentionBond.getMaturityDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
    }

    private void preparePerformanceBondDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        PerformanceBond performanceBond = (PerformanceBond) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(CONTRACT_REFERENCE, performanceBond.getReferenceNumber());
        attachmentParams.put(CONTRACT_DESCRIPTION, performanceBond.getContractDescription());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, performanceBond.getBankGuaranteeEnd());
        attachmentParams.put(IMMEDIATE_MATURITY_DATE, formatDate(performanceBond.getImmediateMaturityDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(BG_EXPIRY, performanceBond.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DT, ACCEPTANCE_WITH_EXPIRY_DATE == performanceBond.getBankGuaranteeEndType() ? formatDate(performanceBond.getFinalMaturityDate(), DATE_TIME_FORMATTER_DD_MM_YYYY)
                : formatDate(performanceBond.getMaturityDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
    }

    private void preparePaymentGuaranteeDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Payment payment = (Payment) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(CONTRACT_REFERENCE, payment.getReferenceNumber());
        attachmentParams.put(CONTRACT_DESCRIPTION, payment.getContractDescription());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, payment.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DT, formatDate(payment.getMaturityDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(BG_EXPIRY, payment.getBankGuaranteeEnd());
    }

    private void prepareAdvancePaymentDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        AdvancePayment advancePayment = (AdvancePayment) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(CONTRACT_REFERENCE, advancePayment.getReferenceNumber());
        attachmentParams.put(CONTRACT_DESCRIPTION, advancePayment.getContractDescription());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, advancePayment.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DT, formatDate(advancePayment.getMaturityDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(BG_ACCOUNT, formatIbanNumber(advancePayment.getAdvancePaymentIBAN()));
        attachmentParams.put(BG_EXPIRY, advancePayment.getBankGuaranteeEnd());

    }

    private void prepareBidBondDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        BidBond bidBond = (BidBond) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(CONTRACT_REFERENCE, bidBond.getReferenceNumber());
        attachmentParams.put(CONTRACT_DESCRIPTION, bidBond.getContractDescription());
        attachmentParams.put(BG_EXPIRY, bidBond.getOtherOptionForPartial());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, bidBond.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DT, formatDate(bidBond.getMaturityDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
    }

    private void prepareCustomFourDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        CustomTypeFour customTypeFour = (CustomTypeFour) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(BG_EXPIRY, customTypeFour.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, customTypeFour.getBankGuaranteeEnd());
        attachmentParams.put(CONTRACT_DESCRIPTION, CUSTOMS);
    }

    private void prepareCustomFiveDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        CustomTypeFive customTypeFive = (CustomTypeFive) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(BG_EXPIRY, customTypeFive.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, customTypeFive.getBankGuaranteeEnd());
        attachmentParams.put(CONTRACT_DESCRIPTION, CUSTOMS);
    }

    private void prepareCustomTwoDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        CustomTypeTwo customTypeTwo = (CustomTypeTwo) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(BG_EXPIRY, customTypeTwo.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, customTypeTwo.getBankGuaranteeEnd());
        attachmentParams.put(CONTRACT_DESCRIPTION, CUSTOMS);
    }


    private void prepareRealEstateDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {

        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        RealEstate realEstate = (RealEstate) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(BG_EXPIRY, realEstate.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, realEstate.getBankGuaranteeEnd());
        attachmentParams.put(CONTRACT_DESCRIPTION, REAL_ESTATE);
    }

    private void prepareCustomOneDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        CustomTypeOne customTypeOne = (CustomTypeOne) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(BG_EXPIRY, customTypeOne.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, customTypeOne.getBankGuaranteeEnd());
        attachmentParams.put(CONTRACT_DESCRIPTION, CUSTOMS);
        attachmentParams.put(REPRESENTING, customTypeOne.getRepresenting());
    }

    private void prepareOVAMDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Ovam ovam = (Ovam) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(CONTRACT_REFERENCE, ovam.getReferenceNumber());
        attachmentParams.put(CONTRACT_START_DATE, formatDate(ovam.getWasteTransportStartDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(OVAM_DUE_DATE, formatDate(ovam.getContractDueDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(CONTRACT_END_DATE, formatDate(ovam.getContractDueDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(OVAM_VALIDITY_DUE_DATE, formatDate(ovam.getContractValidityEndDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(BG_EXPIRY, ovam.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, ovam.getBankGuaranteeEnd());
        attachmentParams.put(CONTRACT_DESCRIPTION, OVAM);
        attachmentParams.put(BG_EXPIRY_DT, formatDate(ovam.getContractValidityEndDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
    }

    private void preparePublicContractDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        PublicContract publicContract = (PublicContract) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(CONTRACT_REFERENCE, publicContract.getReferenceNumber());
        attachmentParams.put(CONTRACT_DESCRIPTION, publicContract.getTitle());
        attachmentParams.put(CONTRACT_AMOUNT, publicContract.getTotalAmount().toPlainString());
        attachmentParams.put(CONTRACT_SIGN_DATE, formatDate(publicContract.getGrantDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, publicContract.getExpiryDate());
        attachmentParams.put(BG_EXPIRY, publicContract.getExpiryDate());
    }

    private void prepareStateLotteryDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        StateLottery stateLottery = (StateLottery) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(CONTRACT_START_DATE, formatDate(stateLottery.getDateOfAgreeInPrinc(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, stateLottery.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY, stateLottery.getBankGuaranteeEnd());
        attachmentParams.put(CONTRACT_SIGN_DT, formatDate(stateLottery.getDateOfAgreeInPrinc(), DATE_TIME_FORMATTER_DD_MM_YYYY));
    }

    private void prepareRentalSpecificDetail(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Rental rental = (Rental) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(CONTRACT_REFERENCE, rental.getReferenceNumber());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, rental.getExpiryDate());
        attachmentParams.put(CONTRACT_STREET, rental.getStreet());
        attachmentParams.put(CONTRACT_ZIP, rental.getPostalCode());
        attachmentParams.put(CONTRACT_CITY, rental.getCity());
        attachmentParams.put(CONTRACT_COUNTRY, rental.getCountry());
        attachmentParams.put(CONTRACT_START_DATE, formatDate(rental.getDateOfSignature(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(BG_EXPIRY_DT, formatDate(rental.getRentalEndDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(CONTRACT_END_DATE, formatDate(rental.getContractEndDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(CONTRACT_SIGN_DATE, formatDate(rental.getDateOfSignature(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(RENTAL_CONTRACT_END_DATE, formatDate(rental.getContractEndDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(BG_EXPIRY, rental.getExpiryDate());
    }

    private void prepareDocumentDetails(Map<String, String> templateSummaryParams, BankGuaranteeRequestData bankGuaranteeRequestData, List<Document> documents) {
        templateSummaryParams.put(DOSSIER_URL, dossierUrl);
        templateSummaryParams.put(DOSSIER_ID, bankGuaranteeRequestData.getDossierInformation().getDossierResponseId());
        templateSummaryParams.put(AGREEMENT_ID, bankGuaranteeRequestData.getDossierInformation().getAgreementDossierResponseId());
        String agreementRequestName = String.format("%s-%s", AGREEMENT_DOSSIER_TYPE_CODE, bankGuaranteeRequestData.getDossierInformation().getAgreementDossierResponseId());
        templateSummaryParams.put(AGREEMENT_REQUEST_NAME, agreementRequestName);
        for (Document document : documents) {
            templateSummaryParams.put(String.join("_", document.getDocumentType().name().toLowerCase(), DOC_ID_TEXT), document.getDocumentId());
        }
    }

    private void prepareDckDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Dck dck = (Dck) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(CONTRACT_DESCRIPTION, dck.getContractDescription());
        attachmentParams.put(CONTRACT_STREET, dck.getBuildingAddress().getStreet());
        attachmentParams.put(CONTRACT_ZIP, dck.getBuildingAddress().getPostalCode());
        attachmentParams.put(CONTRACT_CITY, dck.getBuildingAddress().getCity());
        attachmentParams.put(BG_EXPIRY, dck.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, dck.getBankGuaranteeEnd());

    }

    private void prepareStandardPromiseDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetailsData = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsWalloniaStandardPromise woodsWalloniaStandardPromise = (WoodsWalloniaStandardPromise) JsonUtils.convert(guaranteeDetailsData.getBankGuarantee(), guaranteeDetailsData.getBgCode().getClassType());
        attachmentParams.put(CONTRACT_SIGN_DATE, formatDate(woodsWalloniaStandardPromise.getSaleDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(CONTRACT_CITY, woodsWalloniaStandardPromise.getSalePlace());
        attachmentParams.put(BG_EXPIRY_DT, formatDate(woodsWalloniaStandardPromise.getPromiseEndDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(BG_EXPIRY, woodsWalloniaStandardPromise.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, woodsWalloniaStandardPromise.getBankGuaranteeEnd());
    }

    private void prepareFlandersStandardPromiseDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetailsData = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsFlandersStandardPromise woodsFlandersStandardPromise = (WoodsFlandersStandardPromise) JsonUtils.convert(guaranteeDetailsData.getBankGuarantee(), guaranteeDetailsData.getBgCode().getClassType());
        attachmentParams.put(CONTRACT_SIGN_DATE, formatDate(woodsFlandersStandardPromise.getSaleDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(CONTRACT_CITY, woodsFlandersStandardPromise.getSalePlace());
        attachmentParams.put(BG_EXPIRY_DT, formatDate(woodsFlandersStandardPromise.getPromiseEndDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(BG_EXPIRY, woodsFlandersStandardPromise.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, woodsFlandersStandardPromise.getBankGuaranteeEnd());
    }


    private void prepareBlankPromise(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsBlankPromise woodsBlankPromise = (WoodsBlankPromise) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(LOTSDESCRIPTION, woodsBlankPromise.getLotsDescription());
        attachmentParams.put(PROMISE_END_DATE, formatDate(woodsBlankPromise.getPromiseEndDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(BG_EXPIRY_DT, formatDate(woodsBlankPromise.getPromiseEndDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(BG_EXPIRY, woodsBlankPromise.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, woodsBlankPromise.getBankGuaranteeEnd());
    }

    private void prepareWoodsDischargeDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetailsData = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsDischarge woodsDischarge = (WoodsDischarge) JsonUtils.convert(guaranteeDetailsData.getBankGuarantee(), guaranteeDetailsData.getBgCode().getClassType());
        attachmentParams.put(TRANCHE_AMOUNT_ONE, formatAmount(woodsDischarge.getFirstTransactionAmt()));
        attachmentParams.put(TRANCHE_AMOUNT_TWO, formatAmount(woodsDischarge.getSecondTransactionAmt()));
        attachmentParams.put(TRANCHE_AMOUNT_THREE, formatAmount(woodsDischarge.getThirdTransactionAmt()));
        attachmentParams.put(TRANCHE_AMOUNT_FOUR, formatAmount(woodsDischarge.getFourthTransactionAmt()));
        attachmentParams.put(TRANCHE_DEADLINE_ONE, formatDate(woodsDischarge.getFirstDeadline(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(TRANCHE_DEADLINE_TWO, formatDate(woodsDischarge.getSecondDeadline(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(TRANCHE_DEADLINE_THREE, formatDate(woodsDischarge.getThirdDeadline(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(TRANCHE_DEADLINE_FOUR, formatDate(woodsDischarge.getFourthDeadline(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(CASH, formatAmount(woodsDischarge.getCash()));
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, woodsDischarge.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY, woodsDischarge.getBankGuaranteeEnd());
        attachmentParams.put(CONTRACT_SIGN_DATE, formatDate(woodsDischarge.getSaleDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(CONTRACT_CITY, woodsDischarge.getSalePlace());
        attachmentParams.put(CONTRACT_AMOUNT, formatAmount(woodsDischarge.getSalePrice()));
        attachmentParams.put(LOTSDESCRIPTION, woodsDischarge.getLotsDescription());
        attachmentParams.put(PROMISE_IDS, woodsDischarge.getPromiseIds());
    }

    private void prepareWoodsPrivateSaleDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetailsData = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsPrivate woodsPrivate = (WoodsPrivate) JsonUtils.convert(guaranteeDetailsData.getBankGuarantee(), guaranteeDetailsData.getBgCode().getClassType());
        attachmentParams.put(TRANCHE_AMOUNT_ONE, formatAmount(woodsPrivate.getFirstTransactionAmt()));
        attachmentParams.put(TRANCHE_AMOUNT_TWO, formatAmount(woodsPrivate.getSecondTransactionAmt()));
        attachmentParams.put(TRANCHE_AMOUNT_THREE, formatAmount(woodsPrivate.getThirdTransactionAmt()));
        attachmentParams.put(TRANCHE_AMOUNT_FOUR, formatAmount(woodsPrivate.getFourthTransactionAmt()));
        attachmentParams.put(TRANCHE_DEADLINE_ONE, formatDate(woodsPrivate.getFirstDeadline(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(TRANCHE_DEADLINE_TWO, formatDate(woodsPrivate.getSecondDeadline(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(TRANCHE_DEADLINE_THREE, formatDate(woodsPrivate.getThirdDeadline(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(TRANCHE_DEADLINE_FOUR, formatDate(woodsPrivate.getFourthDeadline(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(CASH, formatAmount(woodsPrivate.getCash()));
        attachmentParams.put(BG_EXPIRY_DT, formatDate(woodsPrivate.getMaturityDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, woodsPrivate.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY, woodsPrivate.getBankGuaranteeEnd());
        attachmentParams.put(CONTRACT_SIGN_DATE, formatDate(woodsPrivate.getSaleDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(CONTRACT_CITY, woodsPrivate.getSalePlace());
        attachmentParams.put(CONTRACT_AMOUNT, formatAmount(woodsPrivate.getSalePrice()));
        attachmentParams.put(LOTSDESCRIPTION, woodsPrivate.getLotsDescription());
        attachmentParams.put(PROMISE_IDS, woodsPrivate.getPromiseIds());
    }

    private void prepareWoodsVLAPublicDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetailsData = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsVLAPublic woodsVLAPublic = (WoodsVLAPublic) JsonUtils.convert(guaranteeDetailsData.getBankGuarantee(), guaranteeDetailsData.getBgCode().getClassType());
        attachmentParams.put(TRANCHE_AMOUNT_ONE, formatAmount(woodsVLAPublic.getFirstTransactionAmt()));
        attachmentParams.put(TRANCHE_AMOUNT_TWO, formatAmount(woodsVLAPublic.getSecondTransactionAmt()));
        attachmentParams.put(TRANCHE_AMOUNT_THREE, formatAmount(woodsVLAPublic.getThirdTransactionAmt()));
        attachmentParams.put(TRANCHE_AMOUNT_FOUR, formatAmount(woodsVLAPublic.getFourthTransactionAmt()));
        attachmentParams.put(TRANCHE_DEADLINE_ONE, formatDate(woodsVLAPublic.getFirstDeadline(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(TRANCHE_DEADLINE_TWO, formatDate(woodsVLAPublic.getSecondDeadline(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(TRANCHE_DEADLINE_THREE, formatDate(woodsVLAPublic.getThirdDeadline(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(TRANCHE_DEADLINE_FOUR, formatDate(woodsVLAPublic.getFourthDeadline(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(CASH, formatAmount(woodsVLAPublic.getCash()));
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, woodsVLAPublic.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY, woodsVLAPublic.getBankGuaranteeEnd());
        attachmentParams.put(CONTRACT_SIGN_DATE, formatDate(woodsVLAPublic.getSaleDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(CONTRACT_CITY, woodsVLAPublic.getSalePlace());
        attachmentParams.put(LOTSDESCRIPTION, woodsVLAPublic.getLotsDescription());
        attachmentParams.put(PROMISE_IDS, woodsVLAPublic.getPromiseIds());

    }

    private void prepareWoodsWALPublicDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetailsData = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsWALPublic woodsWALPublic = (WoodsWALPublic) JsonUtils.convert(guaranteeDetailsData.getBankGuarantee(), guaranteeDetailsData.getBgCode().getClassType());
        attachmentParams.put(TRANCHE_AMOUNT_ONE, formatAmount(woodsWALPublic.getFirstTransactionAmt()));
        attachmentParams.put(TRANCHE_AMOUNT_TWO, formatAmount(woodsWALPublic.getSecondTransactionAmt()));
        attachmentParams.put(TRANCHE_AMOUNT_THREE, formatAmount(woodsWALPublic.getThirdTransactionAmt()));
        attachmentParams.put(TRANCHE_AMOUNT_FOUR, formatAmount(woodsWALPublic.getFourthTransactionAmt()));
        attachmentParams.put(TRANCHE_DEADLINE_ONE, formatDate(woodsWALPublic.getFirstDeadline(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(TRANCHE_DEADLINE_TWO, formatDate(woodsWALPublic.getSecondDeadline(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(TRANCHE_DEADLINE_THREE, formatDate(woodsWALPublic.getThirdDeadline(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(TRANCHE_DEADLINE_FOUR, formatDate(woodsWALPublic.getFourthDeadline(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(CASH, formatAmount(woodsWALPublic.getCash()));
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, woodsWALPublic.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY, woodsWALPublic.getBankGuaranteeEnd());
        attachmentParams.put(CONTRACT_SIGN_DATE, formatDate(woodsWALPublic.getSaleDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(CONTRACT_CITY, woodsWALPublic.getSalePlace());
        attachmentParams.put(CONTRACT_AMOUNT, formatAmount(woodsWALPublic.getSalePrice()));
        attachmentParams.put(LOTSDESCRIPTION, woodsWALPublic.getLotsDescription());
        attachmentParams.put(PROMISE_IDS, woodsWALPublic.getPromiseIds());
    }

    private void prepareGoodsTransportDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetailsData = bankGuaranteeRequestData.getGuaranteeDetails();
        GoodsTransport goodsTransport = (GoodsTransport) JsonUtils.convert(guaranteeDetailsData.getBankGuarantee(), guaranteeDetailsData.getBgCode().getClassType());

        attachmentParams.put(TRANSPORT_LICENSE_NUMBER, goodsTransport.getLicenseNumber());
        attachmentParams.put(BANK_GUARANTEE_ID, goodsTransport.getBankGuaranteeId());
        attachmentParams.put(FOD_GUARANTEE, goodsTransport.getFodGuarantee() != null
                ? getTransportFodGuaranteeType(bankGuaranteeRequestData.getTranslationLanguage().getLanguage(),
                goodsTransport.getFodGuarantee())
                : "N.A");
        attachmentParams.put(CONTRACT_DESCRIPTION, String.valueOf(goodsTransport.getNoOfVehicle()));
        attachmentParams.put(BG_EXPIRY, goodsTransport.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, goodsTransport.getBankGuaranteeEnd());
    }

    private void preparePassengerTransportDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetailsData = bankGuaranteeRequestData.getGuaranteeDetails();
        PassengerTransport passengerTransport = (PassengerTransport) JsonUtils.convert(guaranteeDetailsData.getBankGuarantee(), guaranteeDetailsData.getBgCode().getClassType());

        attachmentParams.put(TRANSPORT_LICENSE_NUMBER, passengerTransport.getLicenseNumber());
        attachmentParams.put(BANK_GUARANTEE_ID, passengerTransport.getBankGuaranteeId());
        attachmentParams.put(FOD_GUARANTEE, passengerTransport.getFodGuarantee() != null
                ? getTransportFodGuaranteeType(bankGuaranteeRequestData.getTranslationLanguage().getLanguage(),
                passengerTransport.getFodGuarantee())
                : "N.A");
        attachmentParams.put(CONTRACT_DESCRIPTION, String.valueOf(passengerTransport.getNoOfVehicle()));
        attachmentParams.put(BG_EXPIRY, passengerTransport.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, passengerTransport.getBankGuaranteeEnd());
    }

    private void prepareOperatorTransportDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetailsData = bankGuaranteeRequestData.getGuaranteeDetails();
        OperatorsTransport operatorsTransport = (OperatorsTransport) JsonUtils.convert(guaranteeDetailsData.getBankGuarantee(), guaranteeDetailsData.getBgCode().getClassType());

        attachmentParams.put(TRANSPORT_LICENSE_NUMBER, operatorsTransport.getLicenseNumber());
        attachmentParams.put(LICENSE_TYPE, operatorsTransport.getTransportOperatorActivityType() != null
                ? getTransportOperatorTranslation(bankGuaranteeRequestData.getTranslationLanguage().getLanguage(),
                operatorsTransport.getTransportOperatorActivityType())
                : "N.A");
        attachmentParams.put(CONTRACT_DESCRIPTION, String.valueOf(operatorsTransport.getNoOfVehicle()));
        attachmentParams.put(BG_EXPIRY, operatorsTransport.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, operatorsTransport.getBankGuaranteeEnd());
    }

    private void prepareAbstractPromiseDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetailsData = bankGuaranteeRequestData.getGuaranteeDetails();
        PromiseAbstract promiseAbstract = (PromiseAbstract) JsonUtils.convert(guaranteeDetailsData.getBankGuarantee(), guaranteeDetailsData.getBgCode().getClassType());
        attachmentParams.put(CONTRACT_DESCRIPTION, promiseAbstract.getContractDescription());
        attachmentParams.put(CONTRACT_REFERENCE, promiseAbstract.getReferenceNumber());
        attachmentParams.put(BG_EXPIRY_DT, formatDate(promiseAbstract.getPromiseEndDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(BG_EXPIRY, promiseAbstract.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, promiseAbstract.getBankGuaranteeEnd());
    }

    private void preparePublicContractPromiseDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetailsData = bankGuaranteeRequestData.getGuaranteeDetails();
        PromisePublicContract promisePublicContract = (PromisePublicContract) JsonUtils.convert(guaranteeDetailsData.getBankGuarantee(), guaranteeDetailsData.getBgCode().getClassType());
        attachmentParams.put(CONTRACT_DESCRIPTION, promisePublicContract.getTitle());
        attachmentParams.put(CONTRACT_REFERENCE, promisePublicContract.getContractDescription());
        attachmentParams.put(BG_EXPIRY_DT, formatDate(promisePublicContract.getPromiseEndDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(BG_EXPIRY, promisePublicContract.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, promisePublicContract.getBankGuaranteeEnd());
    }

    private void prepareCustomizedTextDetails(Map<String, String> attachmentParams, BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        CustomizedText customizedText = (CustomizedText) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        attachmentParams.put(CONTRACT_REFERENCE, customizedText.getReferenceNumber());
        attachmentParams.put(CONTRACT_DESCRIPTION, customizedText.getContractDescription());
        attachmentParams.put(BG_EXPIRY, customizedText.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DESCRIPTION, customizedText.getBankGuaranteeEnd());
        attachmentParams.put(BG_EXPIRY_DT, formatDate(customizedText.getMaturityDate(), DATE_TIME_FORMATTER_DD_MM_YYYY));
        attachmentParams.put(COMMENTS, customizedText.getComments());
        attachmentParams.put(SUB_BG_TYPE, ObjectUtils.isNotEmpty(customizedText.getSubBgCode()) ? customizedText.getSubBgCode().name() : "Other");
        attachmentParams.put(OTHER_OPTION_TEXT, customizedText.getOtherOptionForPartial());
        attachmentParams.put(UPLOADED_FILES, String.join(",", bankGuaranteeRequestData.getCustomDocumentDetails().getUploadedFilesNames()));
    }

    private Optional<String> getAccountNumberToBeDebited(AccountData accountData) {
        return Optional.ofNullable(accountData)
                .map(AccountData::getIbanNumber)
                .map(CommonUtils::formatIbanNumber);
    }

    private Optional<String> getAccountNumberCreditLine(ContractDetailData contractDetailData) {
        return Optional.ofNullable(contractDetailData)
                .map(ContractDetailData::getIbanNumber)
                .map(CommonUtils::formatIbanNumber);
    }

    private Optional<String> getAccountBalanceCreditLine(ContractDetailData contractDetailData) {
        return Optional.ofNullable(contractDetailData)
                .map(ContractDetailData::getAmount)
                .map(BigDecimal::toPlainString);
    }

    private Optional<String> getLegalRepresentativeDigitalAddress(InstructingPartyData.IndividualData individualData, String type) {
        return Optional.ofNullable(individualData.getDigitalAddresses())
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .filter(digitalAddressData -> type.equals(digitalAddressData.getDigitalAddressType()))
                .findFirst()
                .map(InstructingPartyData.DigitalAddressData::getFullDigitalAddress);
    }

    private static boolean checkCreditLineNotAvailableAndSdsIsPossible(StpResultDataSet stpResultDataSet) {
        boolean sdsResult = stpResultDataSet.getStpResultByType(StpCriteriaType.SDS_RESPONSE).map(StpResultDataSet.STPResultData::isStpPossible).orElse(false);
        Optional<CreditLineStatus> creditLineStatus = stpResultDataSet.getStpResultByType(StpCriteriaType.CREDIT_LINE_BALANCE).map(StpResultDataSet.STPResultData::getJustification)
                .map(CreditLineStatus::valueOf);

        return creditLineStatus.isPresent() && (creditLineStatus.get() == CreditLineStatus.NOT_AVAILABLE) && sdsResult;
    }

}
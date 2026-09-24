package com.ing.bankguarantees.remote.rest.connectdot.mapper;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.domain.*;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.BankGuaranteeEndType;
import com.ing.bankguarantees.models.enums.BeneficiaryType;
import com.ing.bankguarantees.models.guaranteetype.PerformanceBond;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.ApplicantDocumentPayload;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.ApplicantDocumentPayload.ApplicantContactDocumentPayload;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.BeneficiaryDocumentPayload;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotInput;
import com.ing.bankguarantees.service.referencedata.CountryDetailService;
import com.ing.bankguarantees.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationHelper.formatDate;
import static com.ing.bankguarantees.utils.CommonUtils.*;
import static com.ing.bankguarantees.utils.ConstantUtils.DATE_TIME_FORMATTER_DD_MM_YYYY;


@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectDotUtils {

    private static final String SPECIFIED_MATURITY_DATE_TEXT = "Specified (maturity date)";
    private static final String REPRESENTING_100_TEXT = "100 % of the reference amount of the global guarantee to which this pledge is linked";
    private static final String BG_FINAL_REFERENCE_TEXT = "<Enter TI+ Reference here>";

    private final CountryDetailService countryService;

    public BeneficiaryDocumentPayload prepareBeneficiary(ConnectDotInput connectDotInput) {
        log.info("ConnectDotUtils [prepareBeneficiary] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();

        if (bankGuaranteeRequestData.getGuaranteeDetails().getBgCode() == BankGuaranteeCode.WOODS_PROM_B)
            return null;

        BeneficiaryData beneficiary = connectDotInput.getBankGuaranteeRequestData().getBeneficiary();
        var beneficiaryDocumentPayload = prepareBeneficiaryDocumentPayload(connectDotInput);

        if (beneficiary.getBeneficiaryType() == BeneficiaryType.PRIVATE_INDIVIDUAL) {
            var primaryBeneficiaryName = beneficiary.getPrivateIndividual().getPrimaryBeneficiaryName();
            var secondaryBeneficiaryName = beneficiary.getPrivateIndividual().getSecondaryBeneficiaryName();
            var primaryBeneficiaryId = beneficiary.getPrivateIndividual().getPrimaryIdentificationReference();
            var secondaryBeneficiaryId = beneficiary.getPrivateIndividual().getSecondaryIdentificationReference();
            var primaryBeneficiaryDob = beneficiary.getPrivateIndividual().getPrimaryBeneficiaryDob();
            var secondaryBeneficiaryDob = beneficiary.getPrivateIndividual().getSecondaryBeneficiaryDob();

            beneficiaryDocumentPayload.setCompanyName(primaryBeneficiaryName);
            beneficiaryDocumentPayload.setId(getBeneficiaryId(primaryBeneficiaryId, primaryBeneficiaryDob));

            if (StringUtils.isNotEmpty(secondaryBeneficiaryName)) {
                beneficiaryDocumentPayload.setSecondaryBeneficiaryName(secondaryBeneficiaryName);
                beneficiaryDocumentPayload.setSecondaryBeneficiaryId(getBeneficiaryId(secondaryBeneficiaryId, secondaryBeneficiaryDob));
            }
        }
        if (beneficiary.getBeneficiaryType() == BeneficiaryType.COMPANY) {
            beneficiaryDocumentPayload.setCompanyName(beneficiary.getOrganisationName().getFullName());
        }
        return beneficiaryDocumentPayload;
    }

    private String getBeneficiaryId(String beneficiaryId, LocalDate beneficiaryDob) {
        return StringUtils.isNotEmpty(beneficiaryId) ? formatReferenceIdNumber(beneficiaryId) : formatDate(beneficiaryDob, DATE_TIME_FORMATTER_DD_MM_YYYY);
    }

    private BeneficiaryDocumentPayload prepareBeneficiaryDocumentPayload(ConnectDotInput connectDotInput) {
        BeneficiaryData beneficiary = connectDotInput.getBankGuaranteeRequestData().getBeneficiary();
        return BeneficiaryDocumentPayload.builder()
                .street(beneficiary.getPostalAddress().getFirstAddress())
                .city(beneficiary.getPostalAddress().getCityName())
                .zip(beneficiary.getPostalAddress().getPostalCode())
                .country(getCountryName(connectDotInput, beneficiary.getPostalAddress().getCountryCode()))
                .email(beneficiary.getEmailAddress().getEmailIdInformation())
                .countryCode(beneficiary.getPostalAddress().getCountryCode())
                .kboNumber(formatKboNumber(beneficiary.getCinNumber()))
                .id(CommonUtils.getBeneficiaryId(connectDotInput.getBankGuaranteeRequestData().getBeneficiary()))
                .build();
    }

    public ApplicantDocumentPayload prepareApplicant(ConnectDotInput connectDotInput) {
        log.info("ConnectDotUtils [prepareApplicant] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        InstructingPartyData.IndividualData individual = bankGuaranteeRequestData.getInstructingParty().getIndividual();
        InstructingPartyData.OrganisationData organisation = bankGuaranteeRequestData.getInstructingParty().getOrganisation();
        boolean issueToAnotherParty = bankGuaranteeRequestData.isIssueToAnotherParty();
        ApplicantData applicant = bankGuaranteeRequestData.getApplicant();
        return ApplicantDocumentPayload.builder()
                .companyName(issueToAnotherParty ? applicant.getOrganisationName().getFullName() : organisation.getOrganisationName().getFullName())
                .street(issueToAnotherParty ? applicant.getPostalAddress().getFirstAddress() : organisation.getPostalAddress().getFirstAddress())
                .city(issueToAnotherParty ? applicant.getPostalAddress().getCityName() : organisation.getPostalAddress().getCityName())
                .zip(issueToAnotherParty ? applicant.getPostalAddress().getPostalCode() : organisation.getPostalAddress().getPostalCode())
                .country(getCountryName(connectDotInput, issueToAnotherParty ? applicant.getPostalAddress().getCountryCode() : organisation.getPostalAddress().getCountryCode()))
                .kboNumber(CommonUtils.formatKboNumber(issueToAnotherParty ? applicant.getCinNumber() : organisation.getCinNumber()))
                .contact(ApplicantContactDocumentPayload.builder()
                        .name(individual.getIndividualName().getFullName())
                        .phone(CommonUtils.getPhoneDigitalAddress(individual.getDigitalAddresses()))
                        .email(getEmailDigitalAddress(individual.getDigitalAddresses()))
                        .build())
                .build();
    }

    public static String getDisplayLanguage(Locale source, Locale destination) {
        return source.getDisplayLanguage(destination);
    }

    private String getCountryName(ConnectDotInput connectDotInput, String countryCode) {
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        return switch (connectDotInput.getDocumentType()) {
            case BG_DRAFT, BG_FINAL ->
                    countryService.getCountryNameByCode(new Locale(bankGuaranteeRequestData.getGuaranteeDetails().getBgLanguage().getLanguageCode()), countryCode);
            case CONTRACT, CONTRACT_FINAL ->
                    countryService.getCountryNameByCode(bankGuaranteeRequestData.getTranslationLanguage(), countryCode);
            case CUSTOMIZED_DOC -> unsupportedOperation(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode());
        };

    }

    public static String getCreditLineAccountNumber(BankGuaranteeRequestData bankGuaranteeRequestData) {
        return Optional.ofNullable(bankGuaranteeRequestData.getFinancialInformation().getCreditLine())
                .map(FinancialInformationData.ContractDetailData::getIbanNumber)
                .map(CommonUtils::formatIbanNumber)
                .orElse(formatIbanNumber(bankGuaranteeRequestData.getFinancialInformation().getAccountToBeDebited().getIbanNumber()));
    }




    public static Double getBgAmountFiftyPercentUp(BigDecimal bgAmount) {
        return Math.ceil((bgAmount.doubleValue() / 2) * 100) / 100;

    }

    public static Double getBgAmountFiftyPercentDown(BigDecimal bgAmount) {
        return Math.floor((bgAmount.doubleValue() / 2) * 100) / 100;  // 86470

    }

    public static LocalDate getPerformanceBondMaturityDate(PerformanceBond performanceBond) {
        return BankGuaranteeEndType.ACCEPTANCE_WITH_EXPIRY_DATE == performanceBond.getBankGuaranteeEndType()
                ? performanceBond.getFinalMaturityDate()
                : performanceBond.getMaturityDate();
    }

    public static String getBankGuaranteeReference(ConnectDotInput connectDotInput) {
        String masterReferenceId = connectDotInput.getMasterReferenceId();
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        return switch (connectDotInput.getDocumentType()) {
            case BG_DRAFT, CONTRACT -> bankGuaranteeRequestData.getReferenceNumber();
            case BG_FINAL, CONTRACT_FINAL ->
                    bankGuaranteeRequestData.isStp() ? masterReferenceId : BG_FINAL_REFERENCE_TEXT;
            case CUSTOMIZED_DOC -> unsupportedOperation(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode());
        };
    }


    public static String getSpecifiedMaturityText(BankGuaranteeEndType bankGuaranteeEndType, String bankGuaranteeEnd) {
        return bankGuaranteeEndType == BankGuaranteeEndType.SPECIFIED
                ? SPECIFIED_MATURITY_DATE_TEXT
                : bankGuaranteeEnd;
    }

    public static String getModelType(BankGuaranteeCode bgCode) {
        return switch (bgCode) {
            case STATE_LOTTERY -> "LOTTO";
            case MONEY_RETENTION_BOND -> "MONEY_RETENTION";
            default -> bgCode.name();
        };
    }

    public static <T> T unsupportedOperation(BankGuaranteeCode bankGuaranteeCode) {
        log.error("Guarantee generation is not allow for  bg type {} ", bankGuaranteeCode);
        throw new BgosException(ErrorCode.TECHNICAL_ERROR);
    }

}

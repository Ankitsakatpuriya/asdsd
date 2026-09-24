package com.ing.bankguarantees.remote.kafka.engagementsuite.mapper;


import com.ing.bankguarantees.models.domain.*;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.BankGuaranteeRecipient;
import com.ing.bankguarantees.models.enums.StpCriteriaType;
import com.ing.bankguarantees.models.guaranteetype.BaseGuaranteeType;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.EmailNotificationInput;
import com.ing.bankguarantees.remote.rest.connectdot.mapper.ConnectDotUtils;
import com.ing.bankguarantees.service.referencedata.CountryDetailService;
import com.ing.bankguarantees.service.referencedata.CurrencyDetailService;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.utils.CommonUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NonStpEmailParamMapperTest {

    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";
    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String BG_STP_RESULT_FILE = "BGA/BGR/bg_stp_result.json";
    private static final String STP_RESULT_TYPE = "type";
    private static final String STP_RESULT_STATUS = "status";
    private static final String STP_RESULT_TIMESTAMP = "timestamp";
    private static final String STP_RESULT_JUSTIFICATION = "justification";
    public static final String APPLICANT_KBO = "applicantKbo";
    public static final String APPLICANT_NAME = "applicantName";
    public static final String APPLICANT_STREET = "applicantStreet";
    public static final String APPLICANT_CITY = "applicantCity";
    public static final String APPLICANT_ZIP = "applicantZip";
    public static final String APPLICANT_COUNTRY = "applicantCountry";
    public static final String SEND_TO = "sendTo";
    public static final String SEND_TO_NAME = "sendToName";
    public static final String SEND_MODE = "sendMode";
    public static final String SEND_TO_COMPANY = "sendToCompany";
    public static final String SEND_TO_STREET = "sendToStreet";
    public static final String SEND_TO_CITY = "sendToCity";
    public static final String SEND_TO_ZIP = "sendToZip";
    public static final String COUNTRY_BELGIUM = "Belgium";
    private static final String DOCUMENT_ID = "344135tplow";

    @Mock
    private CurrencyDetailService currencyDetailService;

    @Mock
    private CountryDetailService countryDetailService;

    @Mock
    private ConnectDotUtils connectDotUtils;

    @InjectMocks
    private NonStpEmailParamMapper nonStpEmailParamMapper;


    @ParameterizedTest
    @MethodSource("provideBankGuaranteeCodes")
    void testParamForAllBgTypes(BankGuaranteeCode bankGuaranteeCodeParam) {

        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(bankGuaranteeCodeParam);
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setGuaranteeDetails(guaranteeDetails);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        bankGuaranteeRequest.getBgRequest().setStpResultDataSet(stpResultDataset);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        EmailNotificationInput emailNotificationInput = MockHelper.getNonStpNotificationInput(bankGuaranteeRequest, List.of(document));
        Map<String, String> resultMap = nonStpEmailParamMapper.prepareAttachmentParameter(emailNotificationInput);
        List<String> stpKeys = getStpKeys();
        assertThat(resultMap).isNotNull();
        assertThat(resultMap).containsKeys(stpKeys.toArray(new String[0]));
    }

    @Test
    void testParamForApplicant() {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setIssueToAnotherParty(true);
        bankGuaranteeRequestData.setStpResultDataSet(stpResultDataset);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        bankGuaranteeRequest.getBgRequest().setStpResultDataSet(stpResultDataset);
        ApplicantData applicant = bankGuaranteeRequestData.getApplicant();
        EmailNotificationInput emailNotificationInput = MockHelper.getNonStpNotificationInput(bankGuaranteeRequest, List.of(document));
        when(countryDetailService.getCountryNameByCode(any(), anyString())).thenReturn(COUNTRY_BELGIUM);
        Map<String, String> resultMap = nonStpEmailParamMapper.prepareAttachmentParameter(emailNotificationInput);
        assertThat(resultMap).isNotNull();
        assertThat(resultMap.get(APPLICANT_KBO)).isEqualTo(CommonUtils.formatKboNumber(applicant.getCinNumber()));
        assertThat(resultMap.get(APPLICANT_CITY)).isEqualTo(applicant.getPostalAddress().getCityName());
        assertThat(resultMap.get(APPLICANT_COUNTRY)).isEqualTo(COUNTRY_BELGIUM);
        assertThat(resultMap.get(APPLICANT_NAME)).isEqualTo(applicant.getOrganisationName().getFullName());
        assertThat(resultMap.get(APPLICANT_STREET)).isEqualTo(applicant.getPostalAddress().getFirstAddress());
        assertThat(resultMap.get(APPLICANT_ZIP)).isEqualTo(applicant.getPostalAddress().getPostalCode());
    }

    @Test
    void testParamForSendToME() {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setIssueToAnotherParty(true);
        bankGuaranteeRequestData.getDeliveryInformation().setRecipient(BankGuaranteeRecipient.ME);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        bankGuaranteeRequest.getBgRequest().setStpResultDataSet(stpResultDataset);
        ApplicantData applicant = bankGuaranteeRequestData.getApplicant();
        EmailNotificationInput emailNotificationInput = MockHelper.getNonStpNotificationInput(bankGuaranteeRequest, List.of(document));
        Map<String, String> resultMap = nonStpEmailParamMapper.prepareAttachmentParameter(emailNotificationInput);
        assertThat(resultMap.get(SEND_TO)).isEqualTo(bankGuaranteeRequestData.getDeliveryInformation().getRecipient().name());
        assertThat(resultMap.get(SEND_MODE)).isEqualTo(bankGuaranteeRequestData.getDeliveryInformation().getMode().getDescription());
        assertThat(resultMap.get(SEND_TO_NAME)).isEqualTo(applicant.getOrganisationName().getFullName());
        assertThat(resultMap.get(SEND_TO_CITY)).isEqualTo(applicant.getPostalAddress().getCityName());
        assertThat(resultMap.get(SEND_TO_COMPANY)).isEqualTo(applicant.getOrganisationName().getFullName());
        assertThat(resultMap.get(SEND_TO_STREET)).isEqualTo(applicant.getPostalAddress().getFirstAddress());
        assertThat(resultMap.get(SEND_TO_ZIP)).isEqualTo(applicant.getPostalAddress().getPostalCode());

    }

    @Test
    void testParamForInstructingPartySendToME() {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.getDeliveryInformation().setRecipient(BankGuaranteeRecipient.ME);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        bankGuaranteeRequest.getBgRequest().setStpResultDataSet(stpResultDataset);
        InstructingPartyData.OrganisationData organisation = bankGuaranteeRequestData.getInstructingParty().getOrganisation();
        EmailNotificationInput emailNotificationInput = MockHelper.getNonStpNotificationInput(bankGuaranteeRequest, List.of(document));
        Map<String, String> resultMap = nonStpEmailParamMapper.prepareAttachmentParameter(emailNotificationInput);
        assertThat(resultMap.get(SEND_TO)).isEqualTo(bankGuaranteeRequestData.getDeliveryInformation().getRecipient().name());
        assertThat(resultMap.get(SEND_MODE)).isEqualTo(bankGuaranteeRequestData.getDeliveryInformation().getMode().getDescription());
        assertThat(resultMap.get(SEND_TO_NAME)).isEqualTo(organisation.getOrganisationName().getFullName());
        assertThat(resultMap.get(SEND_TO_CITY)).isEqualTo(organisation.getPostalAddress().getCityName());
        assertThat(resultMap.get(SEND_TO_COMPANY)).isEqualTo(organisation.getOrganisationName().getFullName());
        assertThat(resultMap.get(SEND_TO_STREET)).isEqualTo(organisation.getPostalAddress().getFirstAddress());
        assertThat(resultMap.get(SEND_TO_ZIP)).isEqualTo(organisation.getPostalAddress().getPostalCode());

    }

    @Test
    void testParamForSendToBeneficiary() {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setIssueToAnotherParty(true);
        bankGuaranteeRequestData.getDeliveryInformation().setRecipient(BankGuaranteeRecipient.BENEFICIARY);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        bankGuaranteeRequest.getBgRequest().setStpResultDataSet(stpResultDataset);
        BeneficiaryData beneficiary = bankGuaranteeRequestData.getBeneficiary();
        EmailNotificationInput emailNotificationInput = MockHelper.getNonStpNotificationInput(bankGuaranteeRequest, List.of(document));
        Map<String, String> resultMap = nonStpEmailParamMapper.prepareAttachmentParameter(emailNotificationInput);
        assertThat(resultMap.get(SEND_TO)).isEqualTo(bankGuaranteeRequestData.getDeliveryInformation().getRecipient().name());
        assertThat(resultMap.get(SEND_MODE)).isEqualTo(bankGuaranteeRequestData.getDeliveryInformation().getMode().getDescription());
        assertThat(resultMap.get(SEND_TO_NAME)).isEqualTo(beneficiary.getOrganisationName().getFullName());
        assertThat(resultMap.get(SEND_TO_CITY)).isEqualTo(beneficiary.getPostalAddress().getCityName());
        assertThat(resultMap.get(SEND_TO_COMPANY)).isEqualTo(beneficiary.getOrganisationName().getFullName());
        assertThat(resultMap.get(SEND_TO_STREET)).isEqualTo(beneficiary.getPostalAddress().getFirstAddress());
        assertThat(resultMap.get(SEND_TO_ZIP)).isEqualTo(beneficiary.getPostalAddress().getPostalCode());

    }

    public static Stream<BankGuaranteeCode> provideBankGuaranteeCodes() {
        return Stream.of(BankGuaranteeCode.values());

    }

    private List<String> getStpKeys() {

        return Arrays.stream(StpCriteriaType.values())
                .filter(stpCriteriaType -> !stpCriteriaType.equals(StpCriteriaType.LGL_REP_COUNT))
                .filter(stpCriteriaType -> stpCriteriaType != StpCriteriaType.LEGAL_REP_CDD)
                .map(stpCriteriaType -> {
                    String prefix = stpCriteriaType.name().toLowerCase();
                    List<String> keys = new ArrayList<>();
                    keys.add(String.join("_", prefix, STP_RESULT_TYPE));
                    keys.add(String.join("_", prefix, STP_RESULT_STATUS));
                    keys.add(String.join("_", prefix, STP_RESULT_JUSTIFICATION));
                    keys.add(String.join("_", prefix, STP_RESULT_TIMESTAMP));
                    return keys;
                })
                .flatMap(Collection::stream)
                .toList();

    }

}

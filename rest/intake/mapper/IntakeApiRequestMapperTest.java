package com.ing.bankguarantees.remote.rest.intake.mapper;

import com.ing.bankguarantees.models.domain.*;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.guaranteetype.BaseGuaranteeType;
import com.ing.bankguarantees.remote.rest.intake.IntakeApiProperties;
import com.ing.bankguarantees.remote.rest.intake.model.request.ApplicationRequest;
import com.ing.bankguarantees.remote.rest.intake.model.request.IntakeApiInput;
import com.ing.bankguarantees.remote.rest.intake.model.request.IntakeApiRequest;
import com.ing.bankguarantees.service.referencedata.CurrencyDetailService;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.ing.bankguarantees.util.MockHelper.getGuaranteeDetails;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class IntakeApiRequestMapperTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String BG_STP_RESULT_FILE = "BGA/BGR/bg_stp_result.json";
    private static final String PERF = "PERF";
    private static final String RENT = "RENT";
    private static final String APAY = "APAY";
    private static final String PAYM = "PAYM";
    private static final String RETE = "RETE";
    private static final String BIDB = "BIDB";
    private static final String CUST = "CUST";
    private static final String DECL = "DECL";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";
    private static final String DOCUMENT_ID = "344135tplow";
    private static final String DEFAULT_COUNTRY_CODE = "BE";

    @Mock
    private IntakeApiProperties intakeApiProperties;

    @Mock
    private CurrencyDetailService currencyService;

    @InjectMocks
    private IntakeApiRequestMapper intakeApiRequestMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(intakeApiRequestMapper, "countryCode", DEFAULT_COUNTRY_CODE);
    }

    @ParameterizedTest
    @MethodSource("bgCodes")
    void preparePegaRequest(BankGuaranteeCode bankGuaranteeCode) {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        bankGuaranteeRequestData.setStpResultDataSet(stpResultDataset);
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = getGuaranteeDetails(bankGuaranteeCode);
        bankGuaranteeRequestData.setGuaranteeDetails(guaranteeDetails);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        IntakeApiInput intakeInput = MockHelper.getIntakeInput(bankGuaranteeRequest, List.of(document));
        IntakeApiRequest intakeApiRequest = intakeApiRequestMapper.prepareIntakeApiRequest(intakeInput);
        assertByCode(bankGuaranteeCode, intakeApiRequest);

    }

    private static Stream<BankGuaranteeCode> bgCodes() {
        return Stream.of(BankGuaranteeCode.values());
    }


    private void assertByCode(BankGuaranteeCode bgCode, IntakeApiRequest intakeApiRequest) {
        switch (bgCode) {
            case PERFORMANCE_BOND -> assertPerformanceBond(intakeApiRequest);
            case RENTAL -> assertRental(intakeApiRequest);
            case ADVANCE_PAYMENT -> assertAdvancePayment(intakeApiRequest);
            case BID_BOND -> assertBidBond(intakeApiRequest);
            case PAYMENT_GUARANTEE -> assertPaymentGuard(intakeApiRequest);
            case MONEY_RETENTION_BOND -> assertMoneyRetentionBond(intakeApiRequest);
            case CUSTOM_1 -> assertCustom1(intakeApiRequest);
            case CUSTOM_2 -> assertCustom2(intakeApiRequest);
            case CUSTOM_4 -> assertCustom4(intakeApiRequest);
            case CUSTOM_5 -> assertCustom5(intakeApiRequest);
            case WOODS_PROM_A -> assertWoodsPromA(intakeApiRequest);
            case OVAM -> assertOvam(intakeApiRequest);
            case DCK_CDC -> assertDCKCdc(intakeApiRequest);
            case STATE_LOTTERY -> assertStateLottery(intakeApiRequest);
            case REAL_ESTATE -> assertRealEstate(intakeApiRequest);
            case PUBLIC_CONTRACT -> assertPublicContract(intakeApiRequest);
            case WOODS_PROM_B -> assertWoodsPromB(intakeApiRequest);
        }
    }

    private void assertWoodsPromB(IntakeApiRequest intakeApiRequest) {

        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(DECL);
    }

    private void assertPublicContract(IntakeApiRequest intakeApiRequest) {
        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(PERF);
    }

    private void assertRealEstate(IntakeApiRequest intakeApiRequest) {
        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(PAYM);
    }

    private void assertStateLottery(IntakeApiRequest intakeApiRequest) {
        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(PAYM);
    }

    private void assertDCKCdc(IntakeApiRequest intakeApiRequest) {
        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(PERF);
    }

    private void assertOvam(IntakeApiRequest intakeApiRequest) {
        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(PERF);
    }

    private void assertWoodsPromA(IntakeApiRequest intakeApiRequest) {
        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(DECL);

    }

    private void assertCustom5(IntakeApiRequest intakeApiRequest) {
        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(CUST);
    }

    private void assertCustom4(IntakeApiRequest intakeApiRequest) {
        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(CUST);
    }

    private void assertCustom2(IntakeApiRequest intakeApiRequest) {
        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(CUST);
    }

    private void assertCustom1(IntakeApiRequest intakeApiRequest) {
        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(CUST);
    }

    private void assertMoneyRetentionBond(IntakeApiRequest intakeApiRequest) {
        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(RETE);
    }

    private void assertPaymentGuard(IntakeApiRequest intakeApiRequest) {
        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(PAYM);
    }

    private void assertBidBond(IntakeApiRequest intakeApiRequest) {
        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(BIDB);
    }

    private void assertAdvancePayment(IntakeApiRequest intakeApiRequest) {
        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(APAY);
    }

    private void assertRental(IntakeApiRequest intakeApiRequest) {
        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(RENT);
    }

    private void assertPerformanceBond(IntakeApiRequest intakeApiRequest) {
        ApplicationRequest.UndertakingTypeRequest undertakingTypeRequest = intakeApiRequest.applicationRequest().undertakingApplications().type();
        assertThat(undertakingTypeRequest.code()).isEqualTo(PERF);

    }


}

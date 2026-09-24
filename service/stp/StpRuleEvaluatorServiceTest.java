package com.ing.bankguarantees.service.stp;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.models.BankGuaranteeDataSet.BankGuaranteeData;
import com.ing.bankguarantees.models.domain.*;
import com.ing.bankguarantees.models.domain.InstructingPartyData.OrganisationData;
import com.ing.bankguarantees.models.domain.StpResultDataSet.STPResultData;
import com.ing.bankguarantees.models.enums.*;
import com.ing.bankguarantees.models.guaranteetype.BaseGuaranteeType;
import com.ing.bankguarantees.models.guaranteetype.BidBond;
import com.ing.bankguarantees.service.bankguarantee.BankGuaranteeService;
import com.ing.bankguarantees.service.documentsigning.SigningEligibilityService;
import com.ing.bankguarantees.service.referencedata.CurrencyDetailService;
import com.ing.bankguarantees.service.stp.creditdecision.CreditDecisionMockService;
import com.ing.bankguarantees.service.stp.creditdecision.CreditDecisionService;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static com.ing.bankguarantees.util.MockHelper.getGuaranteeDetailsData;
import static com.ing.bankguarantees.util.TestConstants.BG_STP_RESULT_FILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class StpRuleEvaluatorServiceTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String DEFAULT_COUNTRY_CODE = "BE";
    private static final String DEFAULT_CURRENCY_VALUE = "003";
    private static final String NEGATIVE_CURRENCY_VALUE = "970";
    private static final String NEGATIVE_CURRENCY_CODE = "AED";
    private static final String NEGATIVE_COUNTRY_CODE = "IN";
    private static final boolean MOCK_CREDIT_DECISION = false;


    @Mock
    private PamQualificationCheckService pamQualificationService;

    @Mock
    private RtnsScreeningEvaluatorService rtnsScreeningService;

    @Mock
    private BankGuaranteeService bankGuaranteeService;

    @Mock
    private CurrencyDetailService currencyService;

    @Mock
    private CreditDecisionService creditDecisionService;

    @Mock
    private CreditDecisionMockService creditDecisionMockService;

    @Mock
    private SigningEligibilityService signingEligibilityService;

    private BankGuaranteeRequestData bankGuaranteeRequestData;

    private StpResultDataSet stpResultDataSet;

    private StpRuleEvaluatorService stpRuleEvaluatorService;

    private AccessToken accessToken;

    @BeforeEach
    void setUp() {
        ExecutorService executorService = ExecutorConfig.workStealingPool();
        accessToken = MockHelper.getAccessToken();
        stpResultDataSet = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        stpRuleEvaluatorService = new StpRuleEvaluatorService(pamQualificationService, rtnsScreeningService, bankGuaranteeService,
                currencyService, executorService, creditDecisionService, creditDecisionMockService, signingEligibilityService);
        ReflectionTestUtils.setField(stpRuleEvaluatorService, "currencyCode", DEFAULT_CURRENCY_VALUE);
        ReflectionTestUtils.setField(stpRuleEvaluatorService, "countryCode", DEFAULT_COUNTRY_CODE);
        ReflectionTestUtils.setField(stpRuleEvaluatorService, "creditDecisionMock", MOCK_CREDIT_DECISION);
        bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setStpResultDataSet(stpResultDataSet);
        OrganisationData organisation = bankGuaranteeRequestData.getInstructingParty().getOrganisation();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        BankGuaranteeData bankGuaranteeData = MockHelper.getBankGuaranteeData(true, guaranteeDetails.getBgCode().name());
        STPResultData pamInstructingStpResult = stpResultDataSet.getStpResultByType(StpCriteriaType.INSTRUCTING_PARTY_CDD).get();
        STPResultData signAllowedStpResult = stpResultDataSet.getStpResultByType(StpCriteriaType.CUSTOMER_SIGN_ALLOWED).get();
        STPResultData signPowerStpResult = stpResultDataSet.getStpResultByType(StpCriteriaType.SIGNER_POWER).get();
        STPResultData sdsStpResult = stpResultDataSet.getStpResultByType(StpCriteriaType.SDS_RESPONSE).get();
        lenient().when(bankGuaranteeService.getMatchedBankGuaranteeData(any())).thenReturn(bankGuaranteeData);
        lenient().when(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency())).thenReturn("EUR");
        lenient().when(rtnsScreeningService.checkNameScreening(any())).thenReturn(CompletableFuture.completedFuture(true));
        lenient().when(signingEligibilityService.allowSigningStpCheck(any())).thenReturn(signAllowedStpResult);
        lenient().when(signingEligibilityService.signingEligibilityCheck(any())).thenReturn(signPowerStpResult);
        lenient().when(pamQualificationService.checkInstructingPartyCddStatus(organisation.getLegalEntityId())).thenReturn(CompletableFuture.completedFuture(pamInstructingStpResult));
        lenient().when(creditDecisionService.checkSdsResponse(any(AccessToken.class), any(BankGuaranteeRequestData.class), anyBoolean())).thenReturn(CompletableFuture.completedFuture(sdsStpResult));
        lenient().when(creditDecisionMockService.checkSDSResponse(any(BigDecimal.class))).thenReturn(CompletableFuture.completedFuture(sdsStpResult));

    }

    @Test
    void CheckCurrencySTPPositive() {
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.CURRENCY_EUR);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.CURRENCY_EUR);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("The selected currency is EUR");


    }


    @Test
    void CheckCurrencySTPNegative() {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        guaranteeDetails.setBgCurrency(NEGATIVE_CURRENCY_VALUE);
        given(currencyService.getCurrencyCodeByValue(NEGATIVE_CURRENCY_VALUE)).willReturn(NEGATIVE_CURRENCY_CODE);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.CURRENCY_EUR);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.CURRENCY_EUR);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo(String.format("The selected currency is %s", NEGATIVE_CURRENCY_CODE));
    }

    @Test
    void CheckPartyFromBelgiumSTPPositive() {
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.PARTY_FROM_BELGIUM);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.PARTY_FROM_BELGIUM);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("All involved party are based in Belgium");
    }

    @Test
    void CheckBeneficiaryNotFromBelgiumSTP() {
        BeneficiaryData beneficiary = bankGuaranteeRequestData.getBeneficiary();
        beneficiary.getPostalAddress().setCountryCode(NEGATIVE_COUNTRY_CODE);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.PARTY_FROM_BELGIUM);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.PARTY_FROM_BELGIUM);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("Beneficiary located outside Belgium");
    }

    @Test
    void CheckInstructingPartyNotFromBelgiumSTP() {

        OrganisationData organisation = bankGuaranteeRequestData.getInstructingParty().getOrganisation();
        organisation.getPostalAddress().setCountryCode(NEGATIVE_COUNTRY_CODE);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.PARTY_FROM_BELGIUM);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.PARTY_FROM_BELGIUM);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("Instructing Party is located outside Belgium");
    }

    @Test
    void CheckApplicantNotFromBelgiumSTP() {

        bankGuaranteeRequestData.setIssueToAnotherParty(true);
        ApplicantData applicant = bankGuaranteeRequestData.getApplicant();
        applicant.getPostalAddress().setCountryCode(NEGATIVE_COUNTRY_CODE);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.PARTY_FROM_BELGIUM);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.PARTY_FROM_BELGIUM);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("Applicant located outside Belgium");
    }

    @Test
    void CheckDeliveryModeEmailSTP() {

        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.DELIVERY_MODE_EMAIL);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.DELIVERY_MODE_EMAIL);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("Email has been selected as mode of delivery");
    }

    @Test
    void CheckDeliveryModeIsNotEmailSTP() {
        bankGuaranteeRequestData.getDeliveryInformation().setMode(BankGuaranteeDeliveryMode.COURIER_SERVICE);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.DELIVERY_MODE_EMAIL);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.DELIVERY_MODE_EMAIL);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("Email has not been selected as mode of delivery");
    }

    @Test
    void CheckBankGuaranteeScopeSTP() {

        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.GUARANTEE_SCOPE);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.GUARANTEE_SCOPE);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("The Guarantee is in scope");
    }

    @Test
    void CheckBankGuaranteeNotInScopeSTP() {
        BankGuaranteeCode bgCode = bankGuaranteeRequestData.getGuaranteeDetails().getBgCode();
        BankGuaranteeData bankGuaranteeData = MockHelper.getBankGuaranteeData(false, bgCode.name());
        given(bankGuaranteeService.getMatchedBankGuaranteeData(bgCode)).willReturn(bankGuaranteeData);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.GUARANTEE_SCOPE);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.GUARANTEE_SCOPE);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("The Guarantee is out of scope");
    }

    @Test
    void CheckApplicantUnavailabilitySTP() {

        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.APPLICANT_UNAVAILABLE);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.APPLICANT_UNAVAILABLE);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("The bank guarantee issue for instructing party");
    }

    @Test
    void CheckApplicantIsAvailableSTP() {

        bankGuaranteeRequestData.setIssueToAnotherParty(true);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.APPLICANT_UNAVAILABLE);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.APPLICANT_UNAVAILABLE);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("The bank guarantee issue for an applicant other than primary party");
    }

    @Test
    void CheckBidBondNotSetToOtherSTP() {

        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.BID_BOND_NOT_OTHER);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.BID_BOND_NOT_OTHER);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("No Bid bond guarantee has been requested ");
    }

    @Test
    void CheckBidBondSetToUnspecifiedSTP() {

        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        guaranteeDetails.setBgCode(BankGuaranteeCode.BID_BOND);
        BankGuaranteeData bankGuaranteeData = MockHelper.getBankGuaranteeData(true, guaranteeDetails.getBgCode().name());
        lenient().when(bankGuaranteeService.getMatchedBankGuaranteeData(guaranteeDetails.getBgCode())).thenReturn(bankGuaranteeData);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.BID_BOND_NOT_OTHER);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.BID_BOND_NOT_OTHER);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("Bid bond expiry selected as Unspecified (with revocation clause)");

    }

    @Test
    void CheckBidBondSetToOtherSTP() {

        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(BankGuaranteeCode.BID_BOND);
        BidBond bankGuarantee = (BidBond) guaranteeDetails.getBankGuarantee();
        bankGuarantee.setBankGuaranteeEndType(BankGuaranteeEndType.OTHER);
        guaranteeDetails.setBgCode(BankGuaranteeCode.BID_BOND);
        guaranteeDetails.setBgAmount(bankGuaranteeRequestData.getGuaranteeDetails().getBgAmount());
        bankGuaranteeRequestData.setGuaranteeDetails(guaranteeDetails);
        BankGuaranteeData bankGuaranteeData = MockHelper.getBankGuaranteeData(true, guaranteeDetails.getBgCode().name());
        lenient().when(bankGuaranteeService.getMatchedBankGuaranteeData(guaranteeDetails.getBgCode())).thenReturn(bankGuaranteeData);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.BID_BOND_NOT_OTHER);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.BID_BOND_NOT_OTHER);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("Bid bond expiry selected as Other");

    }

    @Test
    void CheckCreditLineAvailableWithSufficientSTP() {

        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.CREDIT_LINE_BALANCE);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.CREDIT_LINE_BALANCE);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("SUFFICIENT_BALANCE");
    }

    @Test
    void CheckCreditLineNotAvailableSTP() {
        bankGuaranteeRequestData.getFinancialInformation().setCreditLine(null);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.CREDIT_LINE_BALANCE);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.CREDIT_LINE_BALANCE);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("NOT_AVAILABLE");

    }

    @Test
    void CheckCreditLineAvailableWithInsufficientSTP() {
        bankGuaranteeRequestData.getFinancialInformation().getCreditLine().setAmount(BigDecimal.ONE);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.CREDIT_LINE_BALANCE);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.CREDIT_LINE_BALANCE);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("INSUFFICIENT_BALANCE");

    }

    @Test
    void CheckCreditLineAvailableEndDateWithInsufficientSTP() {
        bankGuaranteeRequestData.getFinancialInformation().getCreditLine().setEndDate(LocalDate.of(2025, 04, 01));
        bankGuaranteeRequestData.setGuaranteeDetails(MockHelper.getGuaranteeDetailsDataForPerformanceBond(bankGuaranteeRequestData));
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.CREDIT_LINE_BALANCE);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.CREDIT_LINE_BALANCE);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("INSUFFICIENT_BALANCE");
    }

    @Test
    void CheckCreditLineAvailableEndDateUnspecifiedWithInsufficientSTP() {
        bankGuaranteeRequestData.getFinancialInformation().getCreditLine().setEndDate(LocalDate.of(2025, 04, 01));
        var performanceBond = MockHelper.getGuaranteeDetailsDataForPerformanceBond(bankGuaranteeRequestData);
        performanceBond.getBankGuarantee().setBankGuaranteeEndType(BankGuaranteeEndType.ACCEPTANCE_WITH_EXPIRY_DATE);
        bankGuaranteeRequestData.setGuaranteeDetails(performanceBond);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.CREDIT_LINE_BALANCE);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.CREDIT_LINE_BALANCE);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("INSUFFICIENT_BALANCE");
    }

    @Test
    void checkCreditLineNonEuroCurrencyWithAvailableNonEurSTP() {
        bankGuaranteeRequestData.getFinancialInformation().getCreditLine().setCurrency(NEGATIVE_CURRENCY_VALUE);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.CREDIT_LINE_BALANCE);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.CREDIT_LINE_BALANCE);
        assertFalse(stpResultOptional.get().isStpPossible());
    }

    @ParameterizedTest
    @MethodSource("insufficientBalanceBgCode")
    void CheckCreditLineAvailableEndDateInsufficientSTP(BankGuaranteeCode bankGuaranteeCode) {
        bankGuaranteeRequestData.getFinancialInformation().getCreditLine().setEndDate(LocalDate.now());
        bankGuaranteeRequestData.setGuaranteeDetails(getGuaranteeDetailsData(bankGuaranteeCode));
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.CREDIT_LINE_BALANCE);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.CREDIT_LINE_BALANCE);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("INSUFFICIENT_BALANCE");
    }

    @ParameterizedTest
    @MethodSource("sufficientBalanceBgCode")
    void CheckCreditLineAvailableEndDatesufficientBalanceBgCode(BankGuaranteeCode bankGuaranteeCode) {
        bankGuaranteeRequestData.getFinancialInformation().getCreditLine().setEndDate(LocalDate.now());
        bankGuaranteeRequestData.setGuaranteeDetails(getGuaranteeDetailsData(bankGuaranteeCode));
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.CREDIT_LINE_BALANCE);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.CREDIT_LINE_BALANCE);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("SUFFICIENT_BALANCE");
    }

    private static Stream<Arguments> insufficientBalanceBgCode() {
        return Stream.of(
                Arguments.of(BankGuaranteeCode.BID_BOND),
                Arguments.of(BankGuaranteeCode.RENTAL),
                Arguments.of(BankGuaranteeCode.ADVANCE_PAYMENT),
                Arguments.of(BankGuaranteeCode.PAYMENT_GUARANTEE),
                Arguments.of(BankGuaranteeCode.MONEY_RETENTION_BOND),
                Arguments.of(BankGuaranteeCode.OVAM),
                Arguments.of(BankGuaranteeCode.WOODS_PROM_VLA),
                Arguments.of(BankGuaranteeCode.WOODS_BG_PRIVATE),
                Arguments.of(BankGuaranteeCode.WOODS_PROM_B),
                Arguments.of(BankGuaranteeCode.WOODS_PROM_A)
        );
    }

    private static Stream<Arguments> sufficientBalanceBgCode() {
        return Stream.of(
                Arguments.of(BankGuaranteeCode.REAL_ESTATE),
                Arguments.of(BankGuaranteeCode.STATE_LOTTERY),
                Arguments.of(BankGuaranteeCode.CUSTOM_5),
                Arguments.of(BankGuaranteeCode.CUSTOM_4),
                Arguments.of(BankGuaranteeCode.CUSTOM_1),
                Arguments.of(BankGuaranteeCode.CUSTOM_2),
                Arguments.of(BankGuaranteeCode.PUBLIC_CONTRACT)

        );
    }

    @Test
    void CheckSdsStatusSTPositive() {
        STPResultData sdsStpResult = stpResultDataSet.getStpResultByType(StpCriteriaType.SDS_RESPONSE).get();
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.SDS_RESPONSE);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get()).isEqualTo(sdsStpResult);
    }

    @Test
    void CheckSdsMockStatusSTPositive() {
        ReflectionTestUtils.setField(stpRuleEvaluatorService, "creditDecisionMock", true);
        STPResultData sdsStpResult = stpResultDataSet.getStpResultByType(StpCriteriaType.SDS_RESPONSE).get();
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.SDS_RESPONSE);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get()).isEqualTo(sdsStpResult);
    }

    @Test
    void CheckBeneficiaryCompanyNameScreeningSTPPositive() {
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.BENEFICIARY_NAME_SCREENING);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.BENEFICIARY_NAME_SCREENING);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("Hit Not Found");
    }

    @Test
    void CheckBeneficiaryPrivateIndividualNameScreeningSTPPositive() {
        BeneficiaryData beneficiary = bankGuaranteeRequestData.getBeneficiary();
        beneficiary.setBeneficiaryType(BeneficiaryType.PRIVATE_INDIVIDUAL);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.BENEFICIARY_NAME_SCREENING);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.BENEFICIARY_NAME_SCREENING);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("Hit Not Found for both primary and secondary beneficiary");
    }

    @Test
    void CheckBeneficiaryNameScreeningSTPNegative() {
        lenient().when(rtnsScreeningService.checkNameScreening(any())).thenReturn(CompletableFuture.completedFuture(false));
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.BENEFICIARY_NAME_SCREENING);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.BENEFICIARY_NAME_SCREENING);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("Hit Found");
    }

    @Test
    void CheckApplicantNameScreeningSTPPositive() {
        bankGuaranteeRequestData.setIssueToAnotherParty(true);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.APPLICANT_NAME_SCREENING);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.APPLICANT_NAME_SCREENING);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("Hit Not Found");
    }

    @Test
    void CheckApplicantNameScreeningSTPNegative() {
        bankGuaranteeRequestData.setIssueToAnotherParty(true);
        lenient().when(rtnsScreeningService.checkNameScreening(any())).thenReturn(CompletableFuture.completedFuture(false));
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.APPLICANT_NAME_SCREENING);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.APPLICANT_NAME_SCREENING);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("Hit Found");
    }

    @Test
    void CheckApplicantAbsentNameScreeningSTP() {
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.APPLICANT_NAME_SCREENING);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.APPLICANT_NAME_SCREENING);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("Applicant not available");
    }

    @Test
    void CheckInstructingPartyCddCheckPassSTP() {
        STPResultData sdsStpResult = stpResultDataSet.getStpResultByType(StpCriteriaType.INSTRUCTING_PARTY_CDD).get();
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.INSTRUCTING_PARTY_CDD);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get()).isEqualTo(sdsStpResult);
    }

    @Test
    void CheckGuaranteeSTPAllowPositive() {
        lenient().when(bankGuaranteeService.getBgSubType(any())).thenReturn("STATE_LOTTERY");
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.GUARANTEE_STP_ALLOWED);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.GUARANTEE_STP_ALLOWED);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("STATE_LOTTERY is allowed to process via STP");
    }

    @ParameterizedTest
    @MethodSource("provideBankGuaranteeCodes")
    void CheckGuaranteeSTPAllowNegative(BankGuaranteeCode bankGuaranteeCode, String subtype) {
        BankGuaranteeData bankGuaranteeData = MockHelper.getBankGuaranteeData(true, subtype);
        given(bankGuaranteeService.getMatchedBankGuaranteeData(bankGuaranteeCode)).willReturn(bankGuaranteeData);
        given(bankGuaranteeService.getBgSubType(any())).willReturn(subtype);
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetailsData = getGuaranteeDetailsData(bankGuaranteeCode);
        bankGuaranteeRequestData.setGuaranteeDetails(guaranteeDetailsData);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.GUARANTEE_STP_ALLOWED);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.GUARANTEE_STP_ALLOWED);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo(String.format("%s is not allowed to process via STP", subtype));
    }

    @Test
    void checkReplacePromisePositive() {
        given(bankGuaranteeService.getBgSubType(any())).willReturn("State Lottery");
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.PROMISE_REPLACER);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.PROMISE_REPLACER);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("State Lottery guarantee is not replacing promise");
    }


    @ParameterizedTest
    @MethodSource("providePromiseReplacer")
    void checkReplacePromiseNegative(BankGuaranteeCode bankGuaranteeCode, String subtype) {
        BankGuaranteeData bankGuaranteeData = MockHelper.getBankGuaranteeData(true, subtype);
        given(bankGuaranteeService.getBgSubType(any())).willReturn(subtype);
        given(bankGuaranteeService.getMatchedBankGuaranteeData(bankGuaranteeCode)).willReturn(bankGuaranteeData);
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetailsData = getGuaranteeDetailsData(bankGuaranteeCode);
        bankGuaranteeRequestData.setGuaranteeDetails(guaranteeDetailsData);
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.PROMISE_REPLACER);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.PROMISE_REPLACER);
        assertFalse(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo(String.format("%s guarantee replacing  promise", subtype));
    }


    @Test
    void checkCustomerSignAllowedPositive() {
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.CUSTOMER_SIGN_ALLOWED);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.CUSTOMER_SIGN_ALLOWED);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("customer is allowed to sign the documents");
    }

    @Test
    void checkSigningPowerPositive() {
        StpResultDataSet expectedDataSet = stpRuleEvaluatorService.performSTPChecks(accessToken, bankGuaranteeRequestData).join();
        Optional<STPResultData> stpResultOptional = expectedDataSet.getStpResultByType(StpCriteriaType.SIGNER_POWER);
        assertThat(stpResultOptional).isPresent();
        assertThat(stpResultOptional.get().getType()).isEqualTo(StpCriteriaType.SIGNER_POWER);
        assertTrue(stpResultOptional.get().isStpPossible());
        assertThat(stpResultOptional.get().getTimestamp()).isNotNull();
        assertThat(stpResultOptional.get().getJustification()).isEqualTo("All signers  have complete authority to sign the documents");
    }


    public static Stream<Arguments> providePromiseReplacer() {
        return Stream.of(
                Arguments.of(BankGuaranteeCode.WOODS_BGWAL_PUBLIC, "Forestry Wallonia with or without cash"),
                Arguments.of(BankGuaranteeCode.WOODS_BG_DISCHARGE, "Forestry Wallonia - Remaining Amount"),
                Arguments.of(BankGuaranteeCode.WOODS_BGVLA_PUBLIC, "Forestry Flanders"));


    }

    private static Stream<Arguments> provideBankGuaranteeCodes() {
        return Stream.of(
                Arguments.of(BankGuaranteeCode.DCK_CDC, "DCK/CDC"),
                Arguments.of(BankGuaranteeCode.WOODS_BG_PRIVATE, "Forestry woods Private")
        );
    }


}

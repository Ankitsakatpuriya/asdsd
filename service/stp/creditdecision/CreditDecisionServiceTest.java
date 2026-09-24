package com.ing.bankguarantees.service.stp.creditdecision;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.GuaranteeDetailsData;
import com.ing.bankguarantees.models.domain.StpResultDataSet.STPResultData;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.StpCriteriaType;
import com.ing.bankguarantees.models.guaranteetype.BaseGuaranteeType;
import com.ing.bankguarantees.models.guaranteetype.WoodsBaseGuaranteeType;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.creditdecision.model.request.CreditDecisionInput;
import com.ing.bankguarantees.remote.rest.creditdecision.model.response.CreditDecisionResponse;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import static com.ing.bankguarantees.models.enums.BankGuaranteeCode.WOODS_BGVLA_PUBLIC;
import static com.ing.bankguarantees.models.enums.BankGuaranteeCode.WOODS_BG_DISCHARGE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class CreditDecisionServiceTest {

    private static final String GREEN_DECISION_SIX = "6";
    private static final String GREEN_DECISION_ONE = "1";
    private static final String RED_DECISION_SEVAN = "7";
    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String DEFAULT_CURRENCY_VALUE = "003";
    private static final String NEGATIVE_CURRENCY_VALUE = "970";
    public final static String NON_EURO_CURRENCY_JUSTIFICATION = "The SDS was not invoked because the bank guarantee amount was in a non EUR currency";
    public final static String PROMISE_JUSTIFICATION = "The SDS was not invoked because the bank guarantee is replacing the promise.";


    @Mock
    private ClientGateway<CreditDecisionInput, CreditDecisionResponse, CreditDecisionResponse> creditRiskGateway;

    @InjectMocks
    private CreditDecisionService creditDecisionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(creditDecisionService, "currencyCode", DEFAULT_CURRENCY_VALUE);
    }


    @ParameterizedTest
    @MethodSource("decisionProvider")
    void checkSdsResponsePositive(String automaticDecision) {

        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        AccessToken accessToken = MockHelper.getAccessToken();
        CreditDecisionResponse creditDecisionResponse = MockHelper.getCreditDecisionResponse(automaticDecision);
        given(creditRiskGateway.performRequestWithValidate(any())).willReturn(CompletableFuture.completedFuture(creditDecisionResponse));
        STPResultData stpResultData = creditDecisionService.checkSdsResponse(accessToken, bankGuaranteeRequestData, true).join();
        assertThat(stpResultData).isNotNull();
        assertThat(stpResultData.isStpPossible()).isTrue();
        assertThat(stpResultData.getType()).isEqualTo(StpCriteriaType.SDS_RESPONSE);
        assertThat(stpResultData.getJustification()).isEqualTo(String.format("Automatic Decision : %s , Simulated : Yes", automaticDecision));

    }

    @Test
    void checkSdsResponseNegative() {

        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        AccessToken accessToken = MockHelper.getAccessToken();
        CreditDecisionResponse creditDecisionResponse = MockHelper.getCreditDecisionResponse(RED_DECISION_SEVAN);
        given(creditRiskGateway.performRequestWithValidate(any())).willReturn(CompletableFuture.completedFuture(creditDecisionResponse));
        STPResultData stpResultData = creditDecisionService.checkSdsResponse(accessToken, bankGuaranteeRequestData, true).join();
        assertThat(stpResultData).isNotNull();
        assertThat(stpResultData.isStpPossible()).isFalse();
        assertThat(stpResultData.getType()).isEqualTo(StpCriteriaType.SDS_RESPONSE);
        assertThat(stpResultData.getJustification()).isEqualTo(String.format("Automatic Decision : %s , Simulated : Yes", RED_DECISION_SEVAN));

    }

    @Test
    void checkSdsResponseNonEurCurrency() {

        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.getGuaranteeDetails().setBgCurrency(NEGATIVE_CURRENCY_VALUE);
        AccessToken accessToken = MockHelper.getAccessToken();
        STPResultData stpResultData = creditDecisionService.checkSdsResponse(accessToken, bankGuaranteeRequestData, true).join();
        assertThat(stpResultData).isNotNull();
        assertThat(stpResultData.isStpPossible()).isFalse();
        assertThat(stpResultData.getType()).isEqualTo(StpCriteriaType.SDS_RESPONSE);
        assertThat(stpResultData.getJustification()).isEqualTo(NON_EURO_CURRENCY_JUSTIFICATION);

    }

    @ParameterizedTest
    @MethodSource("bgCodes")
    void checkSdsResponseReplacePromise(BankGuaranteeCode bankGuaranteeCode) {

        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(bankGuaranteeCode);
        WoodsBaseGuaranteeType woods = (WoodsBaseGuaranteeType) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        woods.setReplacePromise(true);
        bankGuaranteeRequestData.setGuaranteeDetails(guaranteeDetails);
        AccessToken accessToken = MockHelper.getAccessToken();
        STPResultData stpResultData = creditDecisionService.checkSdsResponse(accessToken, bankGuaranteeRequestData, true).join();
        assertThat(stpResultData).isNotNull();
        assertThat(stpResultData.isStpPossible()).isFalse();
        assertThat(stpResultData.getType()).isEqualTo(StpCriteriaType.SDS_RESPONSE);
        assertThat(stpResultData.getJustification()).isEqualTo(PROMISE_JUSTIFICATION);

    }

    @Test
    void checkCddStatusError() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        AccessToken accessToken = MockHelper.getAccessToken();
        given(creditRiskGateway.performRequestWithValidate(any())).willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<STPResultData> stpResultFuture = creditDecisionService.checkSdsResponse(accessToken, bankGuaranteeRequestData, true);
        CompletionException exception = assertThrows(CompletionException.class, stpResultFuture::join);
        Assertions.assertThat(exception).isNotNull();
        Assertions.assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        Assertions.assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }

    private static Stream<String> decisionProvider() {

        return Stream.of(GREEN_DECISION_SIX, GREEN_DECISION_ONE);
    }

    private static Stream<BankGuaranteeCode> bgCodes() {

        return Stream.of(WOODS_BGVLA_PUBLIC, WOODS_BGVLA_PUBLIC, WOODS_BG_DISCHARGE);
    }
}

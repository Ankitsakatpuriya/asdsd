package com.ing.bankguarantees.service.documentsigning;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.bankguarantees.database.BankGuaranteeRequestDao;
import com.ing.bankguarantees.database.DocumentDao;
import com.ing.bankguarantees.database.ReportingDao;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.domain.*;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import com.ing.bankguarantees.models.enums.CreditLineStatus;
import com.ing.bankguarantees.models.enums.FundReservedBy;
import com.ing.bankguarantees.models.enums.StpCriteriaType;
import com.ing.bankguarantees.models.response.SignDocumentResponse;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.request.CreditBalanceInput;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.response.CreditBalanceOutput;
import com.ing.bankguarantees.remote.rest.dar.model.response.DarListResponse.DarResponse;
import com.ing.bankguarantees.remote.rest.holidaycalendar.model.BankHoliday;
import com.ing.bankguarantees.remote.rest.holidaycalendar.model.HolidayCalendarRequest;
import com.ing.bankguarantees.remote.rest.holidaycalendar.model.HolidayCalendarResponse;
import com.ing.bankguarantees.service.financial.FinancialDetailService;
import com.ing.bankguarantees.service.reporting.DataLakeReportingService;
import com.ing.bankguarantees.service.stp.StpRuleEvaluatorService;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.ing.bankguarantees.models.enums.StpCriteriaType.CREDIT_LINE_BALANCE;
import static com.ing.bankguarantees.util.TestConstants.BG_STP_RESULT_FILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentSignatureServiceTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";
    private static final String DAR_UUID = "b59f13b6-0fee-468a-8c7f-c1df1a2b5453";
    private static final BigDecimal INVALID_KLC_NUMBER = new BigDecimal(87654321);
    private static final String DOCUMENT_ID = "344135tplow";

    @Mock
    private StpRuleEvaluatorService stpService;

    @Mock
    private FinancialDetailService financialDetailService;

    @Mock
    private DocumentDao documentDao;

    @Mock
    private DarDigitalSignatureService darDigitalSignatureService;

    @Mock
    private BankGuaranteeRequestDao bankGuaranteeRequestDao;

    @Mock
    private ReportingDao reportingDao;

    @Mock
    private ClientGateway<HolidayCalendarRequest, List<BankHoliday>, HolidayCalendarResponse> holidayCalendarGateway;


    @Mock
    private DataLakeReportingService dataLakeReportingService;

    @InjectMocks
    private DocumentSignatureService documentSignatureService;

    private BankGuaranteeRequestData bankGuaranteeRequestData;
    private StpResultDataSet stpResultDataset;
    private BankGuaranteeRequest bankGuaranteeRequest;


    @BeforeEach
    void setup() {
        bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        List<BankHoliday> bankHolidayList = MockHelper.getBankHolidayList();
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        bankGuaranteeRequest.getBgRequest().setStpResultDataSet(stpResultDataset);
        bankGuaranteeRequest.setStatus(BankGuaranteeRequestStatus.CREATED);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        DarResponse darResponse = MockHelper.getDarResponse(DAR_UUID);
        List<CreditBalanceOutput> creditBalanceOutput = MockHelper.getCreditBalanceOutput(bankGuaranteeRequestData.getFinancialInformation().getCreditLine());
        StpResultDataSet.STPResultData sdsResult = stpResultDataset.getStpResultByType(StpCriteriaType.SDS_RESPONSE).get();
        lenient().when(holidayCalendarGateway.performRequest(any())).thenReturn(CompletableFuture.completedFuture(bankHolidayList));
        lenient().when(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).thenReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        lenient().when(stpService.checkSDSStatus(any(), any(), eq(false))).thenReturn(CompletableFuture.completedFuture(sdsResult));
        lenient().when(financialDetailService.getCreditLines(any(CreditBalanceInput.class))).thenReturn(CompletableFuture.completedFuture(Optional.of(creditBalanceOutput)));
        lenient().when(documentDao.getDocumentsRequestId(any())).thenReturn(CompletableFuture.completedFuture(List.of(document)));
        lenient().when(darDigitalSignatureService.getDarResponse(any())).thenReturn(CompletableFuture.completedFuture(darResponse));
        lenient().when(bankGuaranteeRequestDao.saveBankGuaranteeRequest(any())).thenReturn(bankGuaranteeRequest);
        lenient().when(dataLakeReportingService.sendDataLakeEvent(any())).thenReturn(CompletableFuture.completedFuture(true));
        lenient().doNothing().when(reportingDao).updateStatusByRequestId(any(), any());
    }

    @Test
    void checkInitiateSigningPositive() {
        String requestId = UUID.randomUUID().toString();
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        SignDocumentResponse documentResponse = documentSignatureService.initiateSigning(requestId, accessToken).join();
        assertThat(documentResponse).isNotNull();
        assertThat(documentResponse.getDarId()).isEqualTo(DAR_UUID);
        StpResultDataSet.STPResultData stpResultData = bankGuaranteeRequest.getBgRequest().getStpResultDataSet().getStpResultByType(StpCriteriaType.SDS_RESPONSE).get();
        assertThat(stpResultData.getJustification()).isEqualTo("Automatic Decision : 1");
        assertThat(stpResultData.getTimestamp().toString()).isEqualTo("2025-07-12T08:04:59.691");
        assertThat(stpResultData.isStpPossible()).isEqualTo(true);
        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.SIGN_INITIATED);

    }

    //
    @Test
    void checkForInvalidSigner() {
        String requestId = UUID.randomUUID().toString();
        AccessToken accessToken = MockHelper.getAccessToken();
        CompletableFuture<SignDocumentResponse> signDocFuture = documentSignatureService.initiateSigning(requestId, accessToken);
        CompletionException exception = assertThrows(CompletionException.class, signDocFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-019");
    }

    @Test
    void checkForFailedDocumentGeneration() {
        bankGuaranteeRequest.setStatus(BankGuaranteeRequestStatus.ERROR);
        given(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).willReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        String requestId = UUID.randomUUID().toString();
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        CompletableFuture<SignDocumentResponse> signDocFuture = documentSignatureService.initiateSigning(requestId, accessToken);
        CompletionException exception = assertThrows(CompletionException.class, signDocFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }


    @Test
    void checkForInvalidLegalRepCount() {
        String requestId = UUID.randomUUID().toString();
        List<LegalRepresentativeData> legalRepresentatives = bankGuaranteeRequestData.getLegalRepresentatives();
        bankGuaranteeRequestData.getLegalRepresentatives().add(legalRepresentatives.get(0));
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        given(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).willReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        CompletableFuture<SignDocumentResponse> signDocFuture = documentSignatureService.initiateSigning(requestId, accessToken);
        CompletionException exception = assertThrows(CompletionException.class, signDocFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-018");
    }


    @Test
    void checkForEmptyCreditLine() {
        String requestId = UUID.randomUUID().toString();
        bankGuaranteeRequestData.getFinancialInformation().setCreditLine(null);
        given(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).willReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        SignDocumentResponse documentResponse = documentSignatureService.initiateSigning(requestId, accessToken).join();
        assertThat(documentResponse).isNotNull();
        assertThat(documentResponse.getDarId()).isEqualTo(DAR_UUID);
        verify(stpService, times(1)).checkSDSStatus(any(), any(), eq(false));
        StpResultDataSet.STPResultData stpResultData = bankGuaranteeRequest.getBgRequest().getStpResultDataSet().getStpResultByType(StpCriteriaType.SDS_RESPONSE).get();
        assertThat(stpResultData.getJustification()).isEqualTo("Automatic Decision : 1");
        assertThat(stpResultData.getTimestamp().toString()).isEqualTo("2025-07-12T08:04:59.691");
        assertThat(stpResultData.isStpPossible()).isEqualTo(true);
        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.SIGN_INITIATED);

    }


    @Test
    void checkForInvalidCreditLineAccount() {

        String requestId = UUID.randomUUID().toString();
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        List<CreditBalanceOutput> creditBalanceOutputList = MockHelper.getCreditBalanceOutput(bankGuaranteeRequestData.getFinancialInformation().getCreditLine());
        creditBalanceOutputList.get(0).setKlcNumber(INVALID_KLC_NUMBER);
        lenient().when(financialDetailService.getCreditLines(any(CreditBalanceInput.class))).thenReturn(CompletableFuture.completedFuture(Optional.of(creditBalanceOutputList)));

        CompletableFuture<SignDocumentResponse> signDocFuture = documentSignatureService.initiateSigning(requestId, accessToken);
        CompletionException exception = assertThrows(CompletionException.class, signDocFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }

    @Test
    void checkForCreditLineBalanceLessThanBGAmount() {

        String requestId = UUID.randomUUID().toString();
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequestData.getGuaranteeDetails().setBgAmount(BigDecimal.valueOf(22222222222.22));
        StpResultDataSet.STPResultData stpResultData = stpResultDataset.getStpResultByType(StpCriteriaType.CREDIT_LINE_BALANCE).get();

        SignDocumentResponse documentResponse = documentSignatureService.initiateSigning(requestId, accessToken).join();
        assertThat(documentResponse).isNotNull();
        assertThat(documentResponse.getDarId()).isEqualTo(DAR_UUID);
        assertThat(stpResultData.getJustification()).isEqualTo(CreditLineStatus.INSUFFICIENT_BALANCE.name());
        assertThat(stpResultData.isStpPossible()).isEqualTo(false);
        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.SIGN_INITIATED);
    }

    @Test
    void checkInitiateSigningForMissingCreditDecision() {
        String requestId = UUID.randomUUID().toString();
        bankGuaranteeRequestData.getGuaranteeDetails().setBgAmount(BigDecimal.valueOf(22222222222.22));
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        StpResultDataSet.STPResultData creditLineResult = stpResultDataset.getStpResultByType(StpCriteriaType.CREDIT_LINE_BALANCE).get();
        stpResultDataset.getStpResults().remove(creditLineResult);
        SignDocumentResponse documentResponse = documentSignatureService.initiateSigning(requestId, accessToken).join();
        assertThat(documentResponse).isNotNull();
        assertThat(documentResponse.getDarId()).isEqualTo(DAR_UUID);
        assertThat(creditLineResult.getJustification()).isEqualTo(CreditLineStatus.NOT_AVAILABLE.name());
        assertThat(creditLineResult.isStpPossible()).isEqualTo(false);
        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.SIGN_INITIATED);
    }


    @Test
    void checkInitiateSigningForMissingSDSResult() {
        String requestId = UUID.randomUUID().toString();
        bankGuaranteeRequestData.getGuaranteeDetails().setBgAmount(BigDecimal.valueOf(22222222222.22));
        bankGuaranteeRequestData.getFinancialInformation().setCreditLine(null);
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        StpResultDataSet.STPResultData sdsResult = stpResultDataset.getStpResultByType(StpCriteriaType.SDS_RESPONSE).get();
        stpResultDataset.getStpResults().remove(sdsResult);
        SignDocumentResponse documentResponse = documentSignatureService.initiateSigning(requestId, accessToken).join();
        assertThat(documentResponse).isNotNull();
        assertThat(documentResponse.getDarId()).isEqualTo(DAR_UUID);
        assertThat(sdsResult.getJustification()).isEqualTo("Automatic Decision : 1");
        assertThat(sdsResult.isStpPossible()).isEqualTo(true);
        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.SIGN_INITIATED);
    }


    @Test
    void checkInitiateSigningWithIsolatedFundReservationPositive() {
        String requestId = UUID.randomUUID().toString();
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.getBgRequest().setStp(true);
        bankGuaranteeRequest.getBgRequest().setFundReservedBy(FundReservedBy.ISOLATED);
        SignDocumentResponse documentResponse = documentSignatureService.initiateSigning(requestId, accessToken).join();
        assertThat(documentResponse).isNotNull();
        assertThat(documentResponse.getDarId()).isEqualTo(DAR_UUID);
        StpResultDataSet.STPResultData stpResultData = bankGuaranteeRequest.getBgRequest().getStpResultDataSet().getStpResultByType(StpCriteriaType.SDS_RESPONSE).get();
        assertThat(stpResultData.getJustification()).isEqualTo("Automatic Decision : 1");
        assertThat(stpResultData.getTimestamp().toString()).isEqualTo("2025-07-12T08:04:59.691");
        assertThat(stpResultData.isStpPossible()).isEqualTo(true);
        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.SIGN_INITIATED);
        assertThat(bankGuaranteeRequest.getBgRequest().getFundReservedBy()).isEqualTo(FundReservedBy.ISOLATED);
    }

    @Test
    void checkInitiateSigningWithcreditLineFundReservationPositive() {
        String requestId = UUID.randomUUID().toString();
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        var stpResultByType = bankGuaranteeRequest.getBgRequest().getStpResultDataSet()
                .getStpResultByType(CREDIT_LINE_BALANCE).get();
        stpResultByType.setJustification(CreditLineStatus.SUFFICIENT_BALANCE.toString());
        stpResultByType.setStpPossible(true);
        bankGuaranteeRequest.getBgRequest().setFundReservedBy(FundReservedBy.CREDIT_LINE);
        SignDocumentResponse documentResponse = documentSignatureService.initiateSigning(requestId, accessToken).join();
        assertThat(documentResponse).isNotNull();
        assertThat(documentResponse.getDarId()).isEqualTo(DAR_UUID);
        StpResultDataSet.STPResultData stpResultData = bankGuaranteeRequest.getBgRequest().getStpResultDataSet().getStpResultByType(StpCriteriaType.SDS_RESPONSE).get();
        assertThat(stpResultData.getJustification()).isEqualTo("Automatic Decision : 1");
        assertThat(stpResultData.getTimestamp().toString()).isEqualTo("2025-07-12T08:04:59.691");
        assertThat(stpResultData.isStpPossible()).isEqualTo(true);
        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.SIGN_INITIATED);
        assertThat(bankGuaranteeRequest.getBgRequest().getFundReservedBy()).isEqualTo(FundReservedBy.CREDIT_LINE);
    }

}

package com.ing.bankguarantees.service.finalization;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.bankguarantees.database.BankGuaranteeRequestDao;
import com.ing.bankguarantees.database.ReportingDao;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.FulfilmentInput;
import com.ing.bankguarantees.models.domain.StpResultDataSet;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import com.ing.bankguarantees.models.enums.StpCriteriaType;
import com.ing.bankguarantees.models.request.BankGuaranteeFinalizationPayload;
import com.ing.bankguarantees.models.response.BankGuaranteeFinalizationResponse;
import com.ing.bankguarantees.saac.BankGuaranteeSemAEventsHandler;
import com.ing.bankguarantees.service.financial.FundReservationService;
import com.ing.bankguarantees.service.notification.NotificationService;
import com.ing.bankguarantees.service.reporting.DataLakeReportingService;
import com.ing.bankguarantees.service.stp.StpRuleEvaluatorService;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankGuaranteeFinalizationServiceTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String BG_STP_RESULT_FILE = "BGA/BGR/bg_stp_result.json";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String INVALID_REQUESTER_ID = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Mock
    private BankGuaranteeRequestDao bankGuaranteeRequestDao;

    @Mock
    private FundReservationService fundReservationService;

    @Mock
    private BankGuaranteeSemAEventsHandler bankGuaranteeSemAEventsHandler;

    @Mock
    private BankGuaranteeFulfilmentService bankGuaranteeFulfilmentService;

    @Mock
    private ReportingDao reportingDao;

    @Mock
    private StpRuleEvaluatorService stpRuleEvaluatorService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private DataLakeReportingService dataLakeReportingService;

    @InjectMocks
    private BankGuaranteeFinalizationService bankGuaranteeFinalizationService;


    @BeforeEach
    void setup() {

        Logger bankGuaranteeFullfilmentLogger = (Logger) LoggerFactory.getLogger(BankGuaranteeFinalizationService.class.getName());
        bankGuaranteeFullfilmentLogger.setLevel(Level.DEBUG);
        bankGuaranteeFullfilmentLogger.addAppender(mockAppender);

        Logger bankGuaranteeFulfilmentTest = (Logger) LoggerFactory.getLogger(BankGuaranteeFinalizationServiceTest.class.getName());
        bankGuaranteeFulfilmentTest.setLevel(Level.DEBUG);
        bankGuaranteeFulfilmentTest.addAppender(mockAppender);
    }


    @Test
    void checkFinalizeManuallyPositive() {
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);

        when(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).thenReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        when(bankGuaranteeFulfilmentService.startFulfilment(any())).thenReturn(CompletableFuture.completedFuture(null));

        BankGuaranteeFinalizationResponse bgFinalizeResponse = bankGuaranteeFinalizationService.finalizeManually(accessToken, bankGuaranteeRequest.getRequestId()).join();

        ArgumentCaptor<FulfilmentInput> argumentCaptor = ArgumentCaptor.forClass(FulfilmentInput.class);
        verify(bankGuaranteeFulfilmentService, times(1)).startFulfilment(argumentCaptor.capture());
        FulfilmentInput fulfilmentInput = argumentCaptor.getValue();
        assertThat(bgFinalizeResponse.getRequestId()).isEqualTo(bankGuaranteeRequest.getRequestId());
        assertThat(fulfilmentInput.getBankGuaranteeRequest().getBgRequest().isStp()).isFalse();
        assertThat(fulfilmentInput.getBankGuaranteeRequest().getRequestId()).isEqualTo(bankGuaranteeRequest.getRequestId());
        assertThat(fulfilmentInput.getDocuments()).isNull();

    }

    @Test
    void checkFinalizeManuallyInvalidRequester() {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        AccessToken accessToken = MockHelper.getAccessToken(INVALID_REQUESTER_ID, SESSION_ID, INVALID_REQUESTER_ID);
        when(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).thenReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        doNothing().when(bankGuaranteeSemAEventsHandler).publishSemEvent(any());

        CompletableFuture<BankGuaranteeFinalizationResponse> future = bankGuaranteeFinalizationService.finalizeManually(accessToken, bankGuaranteeRequest.getRequestId());
        CompletionException exception = assertThrows(CompletionException.class, future::join);
        verify(bankGuaranteeSemAEventsHandler, times(1)).publishSemEvent(any());
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");


    }

    @Test
    void checkFinalizeManuallyErrorRequest() {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setStatus(BankGuaranteeRequestStatus.ERROR);
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        when(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).thenReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        BankGuaranteeFinalizationPayload bgFinalizePayload = MockHelper.getBankGuaranteeFinalizationPayload(bankGuaranteeRequest.getRequestId());
        CompletableFuture<BankGuaranteeFinalizationResponse> future = bankGuaranteeFinalizationService.finalizeManually(accessToken, bgFinalizePayload.getRequestId());
        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");

    }

    @Test
    void checkFinalizeManuallyFulfilmentFailure() {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);

        when(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).thenReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        when(bankGuaranteeFulfilmentService.startFulfilment(any())).thenReturn(CompletableFuture.failedFuture(new BgosException(ErrorCode.TECHNICAL_ERROR)));

        BankGuaranteeFinalizationPayload bgFinalizePayload = MockHelper.getBankGuaranteeFinalizationPayload(bankGuaranteeRequest.getRequestId());
        CompletableFuture<BankGuaranteeFinalizationResponse> future = bankGuaranteeFinalizationService.finalizeManually(accessToken, bgFinalizePayload.getRequestId());
        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");


    }

    @Test
    void checkFinalizeAfterSignPositive() {
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        bankGuaranteeRequestData.setStpResultDataSet(stpResultDataset);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        StpResultDataSet.STPResultData applicantResult = stpResultDataset.getStpResultByType(StpCriteriaType.APPLICANT_NAME_SCREENING).get();
        StpResultDataSet.STPResultData beneficiaryResult = stpResultDataset.getStpResultByType(StpCriteriaType.BENEFICIARY_NAME_SCREENING).get();
        when(stpRuleEvaluatorService.checkNameScreeningApplicant(any())).thenReturn(CompletableFuture.completedFuture(applicantResult));
        when(stpRuleEvaluatorService.checkNameScreeningBeneficiary(any())).thenReturn(CompletableFuture.completedFuture(beneficiaryResult));
        when(bankGuaranteeFulfilmentService.startFulfilment(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(bankGuaranteeRequestDao.saveBankGuaranteeRequest(any())).thenReturn(bankGuaranteeRequest);

        bankGuaranteeFinalizationService.finalizeAfterSigned(bankGuaranteeRequest).join();

        ArgumentCaptor<FulfilmentInput> argumentCaptor = ArgumentCaptor.forClass(FulfilmentInput.class);
        verify(bankGuaranteeFulfilmentService, times(1)).startFulfilment(argumentCaptor.capture());
        verify(stpRuleEvaluatorService, times(1)).checkNameScreeningApplicant(any());
        verify(stpRuleEvaluatorService, times(1)).checkNameScreeningBeneficiary(any());
        verify(bankGuaranteeRequestDao, times(1)).saveBankGuaranteeRequest(any());
        FulfilmentInput fulfilmentInput = argumentCaptor.getValue();
        assertThat(fulfilmentInput.getBankGuaranteeRequest().getRequestId()).isEqualTo(bankGuaranteeRequest.getRequestId());
        assertThat(fulfilmentInput.getDocuments()).isNull();

    }

    @Test
    void checkFinalizeAfterSignNonStp() {
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        bankGuaranteeRequestData.setStpResultDataSet(stpResultDataset);
        StpResultDataSet.STPResultData stpResultData = stpResultDataset.getStpResultByType(StpCriteriaType.DELIVERY_MODE_EMAIL).get();
        stpResultData.setStpPossible(false);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);

        when(bankGuaranteeFulfilmentService.startFulfilment(any())).thenReturn(CompletableFuture.completedFuture(null));
        bankGuaranteeFinalizationService.finalizeAfterSigned(bankGuaranteeRequest).join();
        ArgumentCaptor<FulfilmentInput> argumentCaptor = ArgumentCaptor.forClass(FulfilmentInput.class);
        verify(bankGuaranteeFulfilmentService, times(1)).startFulfilment(argumentCaptor.capture());
        FulfilmentInput fulfilmentInput = argumentCaptor.getValue();
        assertThat(fulfilmentInput.getBankGuaranteeRequest().getRequestId()).isEqualTo(bankGuaranteeRequest.getRequestId());
        assertThat(fulfilmentInput.getDocuments()).isNull();

    }

    @Test
    void checkPerformPartialSignPositive() {
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        when(fundReservationService.reserveFund(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(bankGuaranteeRequestDao.saveBankGuaranteeRequest(any())).thenReturn(bankGuaranteeRequest);
        when(dataLakeReportingService.sendDataLakeEvent(any())).thenReturn(CompletableFuture.completedFuture(true));
        doNothing().when(reportingDao).updateStatusByRequestId(any(), any());
        doNothing().when(notificationService).sendPartiallySignedEmail(any());

        bankGuaranteeFinalizationService.performPartialSignedAction(bankGuaranteeRequest).join();

        verify(reportingDao, times(1)).updateStatusByRequestId(any(), any());
        verify(bankGuaranteeRequestDao, times(1)).saveBankGuaranteeRequest(any());
        verify(notificationService, times(1)).sendPartiallySignedEmail(any());
        verify(fundReservationService, times(1)).reserveFund(any());
        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.PARTIALLY_SIGNED);

    }


}

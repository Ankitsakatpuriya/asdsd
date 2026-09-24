package com.ing.bankguarantees.service.documentsigning;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.ing.bankguarantees.database.BankGuaranteeRequestDao;
import com.ing.bankguarantees.database.ReportingDao;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.StpResultDataSet;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import com.ing.bankguarantees.saac.BankGuaranteeSemAEventsHandler;
import com.ing.bankguarantees.service.finalization.BankGuaranteeFinalizationService;
import com.ing.bankguarantees.service.finalization.FulfillmentProperties;
import com.ing.bankguarantees.service.financial.FundReservationService;
import com.ing.bankguarantees.service.notification.NotificationService;
import com.ing.bankguarantees.service.reporting.DataLakeReportingService;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.docsign.CallbackEvent;
import com.ing.docsign.callback.DetailedCallbackEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DarCallbackHandlerServiceTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String BG_STP_RESULT_FILE = "BGA/BGR/bg_stp_result.json";
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String DAR_UUID = "b59f13b6-0fee-468a-8c7f-c1df1a2b5453";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";

    public static final String CANCELLED = "CANCELLED";
    public static final String EXPIRED = "EXPIRED";
    public static final String DONE = "DONE";
    public static final String PARTIALLY_DONE = "PARTIALLY_DONE";
    public static final String DRAFT = "DRAFT";
    public static final String REVOKED = "REVOKED";
    public static final String SIGNER_DONE = "SIGNER_DONE";


    @Mock
    private ReportingDao reportingDao;

    @Mock
    private FundReservationService fundReservationService;

    @Mock
    private BankGuaranteeRequestDao bankGuaranteeRequestDao;

    @Mock
    private BankGuaranteeSemAEventsHandler bankGuaranteeSemAEventsHandler;

    @Mock
    private BankGuaranteeFinalizationService bankGuaranteeFinalizationService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private DataLakeReportingService dataLakeReportingService;

    @InjectMocks
    private DarCallbackHandlerService darCallbackHandlerService;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @BeforeEach
    void setup() {
        FulfillmentProperties fulfilmentProperties = MockHelper.getFulfilmentProperties();
        ReflectionTestUtils.setField(darCallbackHandlerService, "fulfillmentProperties", fulfilmentProperties);
        Logger logger = (Logger) LoggerFactory.getLogger(DarCallbackHandlerService.class.getName());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(mockAppender);

    }

    @Test
    void handledCallbackPositive() {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setRequestId(bankGuaranteeRequest.getRequestId());
        bankGuaranteeRequestData.setDarId(DAR_UUID);
        StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        bankGuaranteeRequestData.setStpResultDataSet(stpResultDataset);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        CallbackEvent callbackEvent = MockHelper.getCallbackEvent(bankGuaranteeRequest.getRequestId(), DONE, DAR_UUID);

        when(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).thenReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        when(bankGuaranteeRequestDao.saveBankGuaranteeRequest(any())).thenReturn(bankGuaranteeRequest);
        when(bankGuaranteeFinalizationService.finalizeAfterSigned(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(fundReservationService.releaseFund(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(dataLakeReportingService.sendDataLakeEvent(any())).thenReturn(CompletableFuture.completedFuture(true));
        doNothing().when(reportingDao).updateStatusByRequestId(any(), any());

        darCallbackHandlerService.handleAfterSignCallback(callbackEvent).join();
        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.SIGNED);
        assertThat(bankGuaranteeRequestData.isSigned()).isEqualTo(true);
        verify(bankGuaranteeRequestDao, times(1)).saveBankGuaranteeRequest(any());
        verify(reportingDao, times(1)).updateStatusByRequestId(any(), any());

        verify(mockAppender, atLeast(1)).doAppend(any());
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Receive callback from Dar")));
    }


    @ParameterizedTest
    @MethodSource("darStatuses")
    void handleAfterSignCallbackPositive(String darStatus) {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setRequestId(bankGuaranteeRequest.getRequestId());
        bankGuaranteeRequestData.setDarId(DAR_UUID);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        CallbackEvent callbackEvent = MockHelper.getCallbackEvent(bankGuaranteeRequest.getRequestId(), darStatus, DAR_UUID);
        when(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).thenReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        when(bankGuaranteeRequestDao.saveBankGuaranteeRequest(any())).thenReturn(bankGuaranteeRequest);
        when(fundReservationService.releaseFund(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(dataLakeReportingService.sendDataLakeEvent(any())).thenReturn(CompletableFuture.completedFuture(true));
        doNothing().when(reportingDao).updateStatusByRequestId(any(), any());

        darCallbackHandlerService.handleAfterSignCallback(callbackEvent).join();
        verify(mockAppender, atLeast(1)).doAppend(any());
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Receive callback from Dar")));

        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.CANCELED);
        assertThat(bankGuaranteeRequestData.isSigned()).isEqualTo(false);
        verify(bankGuaranteeRequestDao, times(1)).saveBankGuaranteeRequest(any());
        verify(reportingDao, times(1)).updateStatusByRequestId(any(), any());

    }

    @Test
    void handleAfterSignCallbackForEmptyDarId() {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        CallbackEvent callbackEvent = MockHelper.getCallbackEvent(bankGuaranteeRequest.getRequestId(), DONE, null);
        when(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).thenReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        doNothing().when(bankGuaranteeSemAEventsHandler).publishSemEvent(any());
        CompletableFuture<Void> future = darCallbackHandlerService.handleAfterSignCallback(callbackEvent);
        CompletionException exception = assertThrows(CompletionException.class, future::join);

        verify(bankGuaranteeSemAEventsHandler, times(1)).publishSemEvent(any());
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");

        verify(mockAppender, atLeast(2)).doAppend(any());
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Receive callback from Dar")));
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.ERROR && arg.getFormattedMessage()
                .contains("dar id not found in doc sign callback.")));
    }

    @Test
    void handleAfterSignCallbackForDarIdMismatch() {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        CallbackEvent callbackEvent = MockHelper.getCallbackEvent(bankGuaranteeRequest.getRequestId(), DONE, DAR_UUID);
        when(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).thenReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        doNothing().when(bankGuaranteeSemAEventsHandler).publishSemEvent(any());
        CompletableFuture<Void> future = darCallbackHandlerService.handleAfterSignCallback(callbackEvent);
        CompletionException exception = assertThrows(CompletionException.class, future::join);

        verify(bankGuaranteeSemAEventsHandler, times(1)).publishSemEvent(any());
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");

        verify(mockAppender, atLeast(2)).doAppend(any());
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Receive callback from Dar")));
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.ERROR && arg.getFormattedMessage()
                .contains("invalid dar id " + DAR_UUID + " receive for agreement id " + bankGuaranteeRequest.getRequestId())));
    }

    @Test
    void handleAfterSignCallbackForFundReservationFailed() {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setRequestId(bankGuaranteeRequest.getRequestId());
        bankGuaranteeRequestData.setDarId(DAR_UUID);
        StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        bankGuaranteeRequestData.setStpResultDataSet(stpResultDataset);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        CallbackEvent callbackEvent = MockHelper.getCallbackEvent(bankGuaranteeRequest.getRequestId(), DONE, DAR_UUID);

        when(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).thenReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        when(bankGuaranteeRequestDao.saveBankGuaranteeRequest(any())).thenReturn(bankGuaranteeRequest);
        when(fundReservationService.releaseFund(any())).thenReturn(CompletableFuture.failedFuture(new ClientException("BGOS-17-01", ErrorSource.AKB, null)));
        doNothing().when(reportingDao).updateStatusAndStpByRequestId(any(), any(), any());
        doNothing().when(notificationService).sendFulfilmentFailure(any());

        CompletableFuture<Void> future = darCallbackHandlerService.handleAfterSignCallback(callbackEvent);
        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        ClientException cause = (ClientException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-17-01");
        assertThat(bankGuaranteeRequest.getBgRequest().getFailureRemarks()).isEqualTo("Technical error occurs in fund reservation");

        verify(notificationService, atLeast(1)).sendFulfilmentFailure(any());
        verify(mockAppender, atLeast(1)).doAppend(any());
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Receive callback from Dar")));


    }

    @Test
    void handleIntermediateCallbackPositive() {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setRequestId(bankGuaranteeRequest.getRequestId());
        bankGuaranteeRequestData.setDarId(DAR_UUID);
        StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        bankGuaranteeRequestData.setStpResultDataSet(stpResultDataset);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        DetailedCallbackEvent callbackEvent = MockHelper.getIntermediateCallbackEvent(bankGuaranteeRequest.getRequestId(), PARTIALLY_DONE, SIGNER_DONE,DAR_UUID);

        when(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).thenReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        when(bankGuaranteeFinalizationService.performPartialSignedAction(any())).thenReturn(CompletableFuture.completedFuture(null));

        darCallbackHandlerService.handleIntermediateCallback(callbackEvent).join();
        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.DRAFT);

        verify(mockAppender, atLeast(1)).doAppend(any());
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Receive intermediate callback from Dar")));


    }

    private static Stream<String> darStatuses() {
        return Stream.of(CANCELLED, EXPIRED, REVOKED);
    }

}

package com.ing.bankguarantees.service.finalization;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.ing.bankguarantees.database.BankGuaranteeRequestDao;
import com.ing.bankguarantees.database.DocumentDao;
import com.ing.bankguarantees.database.ReportingDao;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.logger.ExceptionLogger;
import com.ing.bankguarantees.models.domain.*;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.models.enums.StpCriteriaType;
import com.ing.bankguarantees.models.guaranteetype.BaseGuaranteeType;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.intake.model.request.IntakeApiInput;
import com.ing.bankguarantees.remote.rest.intake.model.response.IntakeApiResponse;
import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseInput;
import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseResponse;
import com.ing.bankguarantees.service.documents.DocumentProcessingService;
import com.ing.bankguarantees.service.documentsigning.GessDigitalSignatureService;
import com.ing.bankguarantees.service.notification.NotificationService;
import com.ing.bankguarantees.service.reporting.DataLakeReportingService;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.ing.bankguarantees.util.MockHelper.getGuaranteeDetails;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankGuaranteeFulfilmentServiceTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String BG_STP_RESULT_FILE = "BGA/BGR/bg_stp_result.json";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";
    private static final String DOCUMENT_ID = "344135tplow";
    private static final String PEGA_CASE_ID = "ING-WB-TFS-WORK TFS-BG-5363";
    private static final String MASTER_REFERENCE_ID = "BEBTFSBGR6000012";

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Mock
    private DocumentProcessingService documentProcessingService;

    @Mock
    private BankGuaranteeRequestDao bankGuaranteeRequestDao;

    @Mock
    private ReportingDao reportingDao;

    @Mock
    private DocumentDao documentDao;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ClientGateway<IntakeApiInput, IntakeApiResponse, IntakeApiResponse> intakeApiGateway;

    @Mock
    private GessDigitalSignatureService gessDigitalSignatureService;

    @Mock
    private ClientGateway<PegaCreateCaseInput, String, PegaCreateCaseResponse> pegaCaseCreationGateway;

    @Mock
    private DataLakeReportingService dataLakeReportingService;

    private BankGuaranteeFulfilmentService bankGuaranteeFulfilmentService;

    private BankGuaranteeRequest bankGuaranteeRequest;


    @BeforeEach
    void setup() {

        bankGuaranteeFulfilmentService = new BankGuaranteeFulfilmentService(documentDao, reportingDao, notificationService, bankGuaranteeRequestDao,
                dataLakeReportingService, documentProcessingService, gessDigitalSignatureService, intakeApiGateway, pegaCaseCreationGateway);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setStpResultDataSet(MockHelper.createStpResultDataset(BG_STP_RESULT_FILE));
        bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setMasterReferenceNumber(MASTER_REFERENCE_ID);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        IntakeApiResponse intakeApiResponse = MockHelper.getIntakeApiResponse(MASTER_REFERENCE_ID, PEGA_CASE_ID);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);

        lenient().when(documentProcessingService.generateFinalDocuments(any())).thenReturn(CompletableFuture.completedFuture(true));
        lenient().when(intakeApiGateway.performRequestWithValidate(any())).thenReturn(CompletableFuture.completedFuture(intakeApiResponse));
        lenient().when(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).thenReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        lenient().when(documentDao.getDocumentsRequestId(any())).thenReturn(CompletableFuture.completedFuture(List.of(document)));
        lenient().when(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).thenReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        lenient().when(gessDigitalSignatureService.signDocumentByBank(any(), any())).thenReturn(CompletableFuture.completedFuture(null));
        lenient().doNothing().when(reportingDao).updateStatusAndStpByRequestId(any(), any(), any());
        lenient().when(dataLakeReportingService.sendDataLakeEvent(any())).thenReturn(CompletableFuture.completedFuture(true));
        lenient().when(bankGuaranteeRequestDao.saveBankGuaranteeRequest(any())).thenReturn(bankGuaranteeRequest);
        lenient().when(bankGuaranteeRequestDao.getBankGuaranteeEntityByMasterReference(MASTER_REFERENCE_ID)).thenReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        lenient().when(pegaCaseCreationGateway.performRequest(any())).thenReturn(CompletableFuture.completedFuture("pegaCaseId"));

        Logger bankGuaranteeFullfilmentLogger = (Logger) LoggerFactory.getLogger(BankGuaranteeFulfilmentService.class.getName());
        bankGuaranteeFullfilmentLogger.setLevel(Level.DEBUG);
        bankGuaranteeFullfilmentLogger.addAppender(mockAppender);

        Logger bankGuaranteeFulfilmentTest = (Logger) LoggerFactory.getLogger(BankGuaranteeFulfilmentServiceTest.class.getName());
        bankGuaranteeFulfilmentTest.setLevel(Level.DEBUG);
        bankGuaranteeFulfilmentTest.addAppender(mockAppender);

        Logger exceptionLogger = (Logger) LoggerFactory.getLogger(ExceptionLogger.class.getName());
        exceptionLogger.setLevel(Level.DEBUG);
        exceptionLogger.addAppender(mockAppender);


    }

    @Test
    void checkStartFulfilmentPositive() {
        bankGuaranteeRequest.getBgRequest().setStp(true);

        Document bgDocument = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        bgDocument.setDocumentType(DocumentType.BG_FINAL);
        Document contractDocument = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        contractDocument.setDocumentType(DocumentType.CONTRACT_FINAL);
        when(documentDao.getDocumentsRequestId(any())).thenReturn(CompletableFuture.completedFuture(List.of(bgDocument, contractDocument)));

        FulfilmentInput fulfilmentInput = MockHelper.getFulfilmentInput(bankGuaranteeRequest, null);
        bankGuaranteeFulfilmentService.startFulfilment(fulfilmentInput).join();

        verify(documentProcessingService, times(1)).generateFinalDocuments(bankGuaranteeRequest);
        verify(gessDigitalSignatureService, times(2)).signDocumentByBank(any(), any());
        verify(mockAppender, atLeast(3)).doAppend(any());
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Start fulfilment for request id : " + bankGuaranteeRequest.getRequestId())));
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Case creation initiated successfully for pega case id " + PEGA_CASE_ID + ".")));
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Fulfilment Process initiated successfully for request id " + bankGuaranteeRequest.getRequestId() + " and pega case Id " + PEGA_CASE_ID)));

        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.FULFILMENT_STARTED);
        assertThat(bankGuaranteeRequest.getPegaCaseId()).isEqualTo(PEGA_CASE_ID);
    }

    @Test
    void checkStartFulfilmentNonSTPPositive() {
        FulfilmentInput fulfilmentInput = MockHelper.getFulfilmentInput(bankGuaranteeRequest, null);
        bankGuaranteeFulfilmentService.startFulfilment(fulfilmentInput).join();

        verify(documentProcessingService, times(1)).generateFinalDocuments(bankGuaranteeRequest);
        verify(gessDigitalSignatureService, never()).signDocumentByBank(any(), any());
        verify(notificationService, times(1)).sendFulfilmentFailure(any());
        verify(mockAppender, atLeast(4)).doAppend(any());
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Start fulfilment for request id : " + bankGuaranteeRequest.getRequestId())));
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Case creation initiated successfully for pega case id " + PEGA_CASE_ID + ".")));

        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Bank Guarantee request processed successfully for request data")));

        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Bank Guarantee request processed successfully for request id " + bankGuaranteeRequest.getRequestId())));

        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.FULFILLED);
        assertThat(bankGuaranteeRequest.getPegaCaseId()).isEqualTo(PEGA_CASE_ID);
    }

    @Test
    void checkStartFulfilmentDocGenerationFailed() {
        when(documentProcessingService.generateFinalDocuments(any())).thenReturn(CompletableFuture.completedFuture(false));
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        FulfilmentInput fulfilmentInput = MockHelper.getFulfilmentInput(bankGuaranteeRequest, List.of(document));
        CompletableFuture<Void> future = bankGuaranteeFulfilmentService.startFulfilment(fulfilmentInput);

        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-023");

        assertThat(bankGuaranteeRequest.getPegaCaseId()).isBlank();

        verify(documentProcessingService, times(1)).generateFinalDocuments(bankGuaranteeRequest);
        verify(mockAppender, atLeast(2)).doAppend(any());
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Start fulfilment for request id : " + bankGuaranteeRequest.getRequestId())));
    }

    @Test
    void checkStartFulfilmentForSDSRed() {

        BankGuaranteeRequestData bankGuaranteeRequestData = bankGuaranteeRequest.getBgRequest();
        StpResultDataSet.STPResultData stpResultData = bankGuaranteeRequestData.getStpResultDataSet().getStpResultByType(StpCriteriaType.SDS_RESPONSE).get();
        stpResultData.setStpPossible(false);
        bankGuaranteeRequestData.getFinancialInformation().setCreditLine(null);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        FulfilmentInput fulfilmentInput = MockHelper.getFulfilmentInput(bankGuaranteeRequest, List.of(document));
        bankGuaranteeFulfilmentService.startFulfilment(fulfilmentInput).join();

        verify(documentProcessingService, times(1)).generateFinalDocuments(bankGuaranteeRequest);
        verify(notificationService, times(1)).sendNonStpEmail(any(), any());
        verify(mockAppender, atLeast(3)).doAppend(any());
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Start fulfilment for request id : " + bankGuaranteeRequest.getRequestId())));

        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Bank Guarantee request processed successfully for request data")));

        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Bank Guarantee request processed successfully for request id " + bankGuaranteeRequest.getRequestId())));

        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.FULFILLED);
        assertThat(bankGuaranteeRequest.getPegaCaseId()).isNull();
    }

    @Test
    void checkStartFulfilmentForTransportAmendCase() {
        BankGuaranteeRequestData bankGuaranteeRequestData = bankGuaranteeRequest.getBgRequest();
        StpResultDataSet.STPResultData stpResultData = bankGuaranteeRequestData.getStpResultDataSet().getStpResultByType(StpCriteriaType.SDS_RESPONSE).get();
        stpResultData.setStpPossible(false);
        bankGuaranteeRequestData.getFinancialInformation().setCreditLine(null);
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = getGuaranteeDetails(BankGuaranteeCode.GOODS_TRANSPORT);
        bankGuaranteeRequestData.setGuaranteeDetails(guaranteeDetails);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        FulfilmentInput fulfilmentInput = MockHelper.getFulfilmentInput(bankGuaranteeRequest, List.of(document));

        bankGuaranteeFulfilmentService.startFulfilment(fulfilmentInput).join();

        verify(documentProcessingService, times(1)).generateFinalDocuments(bankGuaranteeRequest);
        verify(notificationService, times(1)).sendStpResultsNotification(any());
        verify(mockAppender, atLeast(1)).doAppend(any());
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Start fulfilment for request id : " + bankGuaranteeRequest.getRequestId())));

        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Bank Guarantee request processed successfully for request data")));

        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Bank Guarantee request processed successfully for request id " + bankGuaranteeRequest.getRequestId())));

        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.FULFILLED);
        assertThat(bankGuaranteeRequest.getPegaCaseId()).isNotNull().isEqualTo("pegaCaseId");
    }

    @Test
    void checkStartFulfilmentDocError() {
        lenient().when(intakeApiGateway.performRequestWithValidate(any())).thenReturn(CompletableFuture.completedFuture(null));
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        FulfilmentInput fulfilmentInput = MockHelper.getFulfilmentInput(bankGuaranteeRequest, List.of(document));

        CompletableFuture<Void> future = bankGuaranteeFulfilmentService.startFulfilment(fulfilmentInput);
        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");

        verify(documentProcessingService, times(1)).generateFinalDocuments(bankGuaranteeRequest);
        verify(mockAppender, atLeast(1)).doAppend(any());
        verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Start fulfilment for request id : " + bankGuaranteeRequest.getRequestId())));
        assertThat(bankGuaranteeRequest.getPegaCaseId()).isBlank();
    }

    @Test
    public void checkProcessTiSuccessFulfilmentPositive() {
        BankGuaranteeRequestData bankGuaranteeRequestData = bankGuaranteeRequest.getBgRequest();
        bankGuaranteeRequestData.setStp(true);
        bankGuaranteeFulfilmentService.processTiSuccessFulfilment(MASTER_REFERENCE_ID).join();

        ArgumentCaptor<String> stpResult = ArgumentCaptor.forClass(String.class);

        verify(notificationService, times(1)).sendStpNotification(any(), any());
        verify(bankGuaranteeRequestDao, times(1)).saveBankGuaranteeRequest(any());
        verify(reportingDao, times(1)).updateStatusAndStpByRequestId(any(), any(), stpResult.capture());
        String stpStatus = stpResult.getValue();

        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.FULFILLED);
        assertThat(stpStatus).isEqualTo("YES");
    }


    @Test
    public void checkProcessTiSuccessFulfilmentForNonStp() {
        bankGuaranteeFulfilmentService.processTiSuccessFulfilment(MASTER_REFERENCE_ID).join();
        verify(notificationService, never()).sendStpNotification(any(), any());
        verify(bankGuaranteeRequestDao, never()).saveBankGuaranteeRequest(any());
        verify(reportingDao, never()).updateStatusAndStpByRequestId(any(), any(), any());
    }

    @Test
    public void checkProcessTiFailedFulfilmentSTP() {

        bankGuaranteeFulfilmentService.processTiFailedFulfilment(bankGuaranteeRequest).join();

        ArgumentCaptor<String> stpResult = ArgumentCaptor.forClass(String.class);
        verify(notificationService, times(1)).sendFulfilmentFailure(any());
        verify(bankGuaranteeRequestDao, times(1)).saveBankGuaranteeRequest(any());
        verify(reportingDao, times(1)).updateStatusAndStpByRequestId(any(), any(), stpResult.capture());

        String stpStatus = stpResult.getValue();
        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.FULFILMENT_ERROR);
        assertThat(stpStatus).isEqualTo("NO");
    }

    @Test
    public void checkProcessTiFailedFulfilmentNonSTP() {
        bankGuaranteeRequest.getBgRequest().setStp(true);
        bankGuaranteeFulfilmentService.processTiFailedFulfilment(bankGuaranteeRequest).join();

        ArgumentCaptor<String> stpResult = ArgumentCaptor.forClass(String.class);
        verify(notificationService, times(1)).sendFulfilmentFailure(any());
        verify(bankGuaranteeRequestDao, times(1)).saveBankGuaranteeRequest(any());
        verify(reportingDao, times(1)).updateStatusAndStpByRequestId(any(), any(), stpResult.capture());

        String stpStatus = stpResult.getValue();
        assertThat(bankGuaranteeRequest.getStatus()).isEqualTo(BankGuaranteeRequestStatus.FULFILMENT_ERROR);
        assertThat(stpStatus).isEqualTo("YES");
    }


}

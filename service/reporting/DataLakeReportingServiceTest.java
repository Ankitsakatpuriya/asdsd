package com.ing.bankguarantees.service.reporting;

import com.ing.bankguarantees.database.BankGuaranteeRequestDao;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.StpResultDataSet;
import com.ing.bankguarantees.remote.kafka.datalakeevent.producer.BankGuaranteeDataLakeEventProducer;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@ExtendWith(MockitoExtension.class)
class DataLakeReportingServiceTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String BG_STP_RESULT_FILE = "BGA/BGR/bg_stp_result.json";
    public static final String SESSION_ID = UUID.randomUUID().toString();
    public static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    public static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    public static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";

    @Mock
    private BankGuaranteeRequestDao bankGuaranteeRequestDao;

    @Mock
    private BankGuaranteeDataLakeEventProducer dataLakeEventProducer;

    @InjectMocks
    private DataLakeReportingService dataLakeReportingService;


    @Test
    void sendDataLakeEvents() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        StpResultDataSet stpResultDataSet = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        bankGuaranteeRequest.getBgRequest().setStpResultDataSet(stpResultDataSet);
        Instant now = Instant.now();
        given(bankGuaranteeRequestDao.getBankGuaranteeEntitiesUpdatedBefore(now))
                .willReturn(CompletableFuture.completedFuture(List.of(bankGuaranteeRequest)));
        given(dataLakeEventProducer.notify(any())).willReturn(CompletableFuture.completedFuture(true));
        dataLakeReportingService.sendDataLakeEvents(now).join();
        verify(bankGuaranteeRequestDao, times(1)).getBankGuaranteeEntitiesUpdatedBefore(any());
        verify(dataLakeEventProducer, times(1)).notify(any());
    }

    @Test
    void sendDataLakeEventsNegative() {
        Instant now = Instant.now();
        given(bankGuaranteeRequestDao.getBankGuaranteeEntitiesUpdatedBefore(now))
                .willReturn(CompletableFuture.failedFuture(new BgosException(ErrorCode.ENTITY_NOT_FOUND)));
        CompletableFuture<Void> voidCompletableFuture = dataLakeReportingService.sendDataLakeEvents(now);
        CompletionException exception = assertThrows(CompletionException.class, voidCompletableFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-007");
        verify(bankGuaranteeRequestDao, times(1)).getBankGuaranteeEntitiesUpdatedBefore(any());
    }

    @Test
    void sendDataLakeEventPositive() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        StpResultDataSet stpResultDataSet = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        bankGuaranteeRequest.getBgRequest().setStpResultDataSet(stpResultDataSet);
        given(bankGuaranteeRequestDao.findBankGuaranteeRequestByRequestId(any())).willReturn(Optional.of(bankGuaranteeRequest));
        given(dataLakeEventProducer.notify(any())).willReturn(CompletableFuture.completedFuture(true));
        dataLakeReportingService.sendDataLakeEvent(bankGuaranteeRequest.getRequestId()).join();
        verify(bankGuaranteeRequestDao, times(1)).findBankGuaranteeRequestByRequestId(any());
        verify(dataLakeEventProducer, times(1)).notify(any());
    }

    @Test
    void sendDataLakeEventNegative() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        StpResultDataSet stpResultDataSet = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        bankGuaranteeRequest.getBgRequest().setStpResultDataSet(stpResultDataSet);
        given(bankGuaranteeRequestDao.findBankGuaranteeRequestByRequestId(any())).willReturn(Optional.of(bankGuaranteeRequest));
        given(dataLakeEventProducer.notify(any())).willReturn(CompletableFuture.failedFuture(new BgosException(ErrorCode.TECHNICAL_ERROR)));
        CompletableFuture<Boolean> future = dataLakeReportingService.sendDataLakeEvent(bankGuaranteeRequest.getRequestId());
        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
        verify(bankGuaranteeRequestDao, times(1)).findBankGuaranteeRequestByRequestId(any());
        verify(dataLakeEventProducer, times(1)).notify(any());
    }


}

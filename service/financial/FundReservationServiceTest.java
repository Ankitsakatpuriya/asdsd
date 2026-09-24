package com.ing.bankguarantees.service.financial;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.StpResultDataSet;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import com.ing.bankguarantees.models.enums.FundReservedBy;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.amsklc.booking.response.BookingRequest;
import com.ing.bankguarantees.remote.rest.amsklc.booking.response.BookingResponse;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.model.request.CreditLineArrangementRequest;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.model.response.CreditLineArrangementOutput;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ing.bankguarantees.util.TestConstants.BG_STP_RESULT_FILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@Slf4j
@ExtendWith(MockitoExtension.class)
class FundReservationServiceTest {

    @InjectMocks
    private FundReservationService fundReservationService;

    @Mock
    private ClientGateway<BookingRequest, String, BookingResponse> bookingClientGateway;

    @Mock
    private ClientGateway<CreditLineArrangementRequest, String, CreditLineArrangementOutput> creditLineArrangementClientGateway;

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";

    private BankGuaranteeRequestData bankGuaranteeRequestData;
    private StpResultDataSet stpResultDataset;
    private BankGuaranteeRequest bankGuaranteeRequest;
    @Mock
    @Qualifier("workStealingPool")
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.getFinancialInformation().getAccountToBeDebited().setPanNumber("1234567890");
        stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        bankGuaranteeRequest.getBgRequest().setStpResultDataSet(stpResultDataset);
        bankGuaranteeRequest.setStatus(BankGuaranteeRequestStatus.CREATED);
        executorService = Executors.newFixedThreadPool(1);
        fundReservationService = new FundReservationService(bookingClientGateway, creditLineArrangementClientGateway);
    }

    @Test
    void reserveFundForIsolatedOperationPostitive() {
        ReflectionTestUtils.setField(fundReservationService, "reserveFund", true);
        given(creditLineArrangementClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture("OK"));
        fundReservationService.reserveFund(bankGuaranteeRequest).join();
        assertThat(bankGuaranteeRequest.getBgRequest().getFundReservedBy()).isEqualTo(FundReservedBy.ISOLATED);
    }

    @Test
    void reserveFundForIsolatedOperationNegative() {
        ReflectionTestUtils.setField(fundReservationService, "reserveFund", true);
        given(creditLineArrangementClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(""));
        var status = fundReservationService.reserveFund(bankGuaranteeRequest);
        CompletionException completionException = assertThrows(CompletionException.class, status::join);
        assertThat(completionException).isNotNull();
        assertThat(completionException.getMessage()).isEqualTo("com.ing.bankguarantees.error.exception.BgosException: BGOS-00-001");
    }

    @Test
    void releaseFundForIsolatedOperationPostitive() {
        ReflectionTestUtils.setField(fundReservationService, "reserveFund", true);
        given(creditLineArrangementClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture("OK"));
        bankGuaranteeRequest.getBgRequest().setFundReservedBy(FundReservedBy.ISOLATED);
        var status = fundReservationService.releaseFund(bankGuaranteeRequest).join();
        assertNotNull(status);
        assertThat(status).isEqualTo("OK");

    }

    @Test
    void releaseFundForCreditLineOperationPostitive() {
        ReflectionTestUtils.setField(fundReservationService, "reserveFund", true);
        given(bookingClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture("OK"));
        bankGuaranteeRequest.getBgRequest().setFundReservedBy(FundReservedBy.CREDIT_LINE);
        var status = fundReservationService.releaseFund(bankGuaranteeRequest).join();
        assertNotNull(status);
        assertThat(status).isEqualTo("OK");
    }


}

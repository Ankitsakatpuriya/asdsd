package com.ing.bankguarantees.service.cleanup;

import com.ing.bankguarantees.database.BankGuaranteeRequestDao;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CleanupJobCoordinatorServiceTest {

    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";
    private static final String REQUESTER_ID_2 = "e22274af-5eea-44c4-88fd-a76b1b3a1111";

    @Mock
    private BankGuaranteeRequestDao bankGuaranteeRequestDao;

    @Mock
    private BankGuaranteeRequestCleanerService bankGuaranteeRequestCleanerService;

    @InjectMocks
    private CleanupJobCoordinatorService cleanupJobCoordinatorService;


    @Test
    void cleanupBankGuaranteeRequestTest() {
        Instant updatedAt = Instant.now().minusSeconds(3600);
        List<BankGuaranteeRequestStatus> statusList = List.of(BankGuaranteeRequestStatus.DRAFT, BankGuaranteeRequestStatus.CREATED);
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        when(bankGuaranteeRequestDao.getBankGuaranteeEntitiesByStatusAndUpdatedAt(any(), any())).thenReturn(CompletableFuture.completedFuture(List.of(bankGuaranteeRequest)));
        when(bankGuaranteeRequestCleanerService.cleanup(bankGuaranteeRequest)).thenReturn(CompletableFuture.completedFuture(1));
        cleanupJobCoordinatorService.cleanupBankGuaranteeRequest(statusList, updatedAt).join();
        verify(bankGuaranteeRequestDao).getBankGuaranteeEntitiesByStatusAndUpdatedAt(statusList, updatedAt);
        verify(bankGuaranteeRequestCleanerService, times(1)).cleanup(bankGuaranteeRequest);
    }

    @Test
    void cleanupBankGuaranteeRequestFailureTest() {
        Instant updatedAt = Instant.now().minusSeconds(3600);
        List<BankGuaranteeRequestStatus> statusList = List.of(BankGuaranteeRequestStatus.DRAFT, BankGuaranteeRequestStatus.CREATED);
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);

        when(bankGuaranteeRequestDao.getBankGuaranteeEntitiesByStatusAndUpdatedAt(statusList, updatedAt)).thenReturn(CompletableFuture.completedFuture(List.of(bankGuaranteeRequest)));
        when(bankGuaranteeRequestCleanerService.cleanup(bankGuaranteeRequest)).thenReturn(CompletableFuture.failedFuture(new BgosException(ErrorCode.TECHNICAL_ERROR)));
        CompletableFuture<Void> future = cleanupJobCoordinatorService.cleanupBankGuaranteeRequest(statusList, updatedAt);
        CompletionException exception = Assertions.assertThrows(CompletionException.class, future::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
        verify(bankGuaranteeRequestDao,times(1)).getBankGuaranteeEntitiesByStatusAndUpdatedAt(statusList, updatedAt);
        verify(bankGuaranteeRequestCleanerService,times(1)).cleanup(bankGuaranteeRequest);
    }

}
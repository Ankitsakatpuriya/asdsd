package com.ing.bankguarantees.service.cleanup;

import com.ing.bankguarantees.database.BankGuaranteeRequestDao;
import com.ing.bankguarantees.database.DocumentDao;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.service.dossier.CaseClosureService;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankGuaranteeRequestCleanerServiceTest {

    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";
    private static final String UPDATE_REQUEST_DOSSIER_RESPONSE_ID = "344135tplov";


    @Mock
    private BankGuaranteeRequestDao bankGuaranteeRequestDao;

    @Mock
    private DocumentDao documentDao;

    @Mock
    private CaseClosureService caseClosureService;

    private BankGuaranteeRequestCleanerService cleanerService;

    @BeforeEach
    void setUp() {
        cleanerService = new BankGuaranteeRequestCleanerService(bankGuaranteeRequestDao, documentDao, caseClosureService);
    }

    @Test
    void cleanupRequestTest() {
        BankGuaranteeRequest dto = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        when(caseClosureService.closeCase(dto)).thenReturn(CompletableFuture.completedFuture(UPDATE_REQUEST_DOSSIER_RESPONSE_ID));
        when(documentDao.deleteByRequestId(dto.getRequestId())).thenReturn(CompletableFuture.completedFuture(1));
        when(bankGuaranteeRequestDao.deleteByRequestId(dto.getRequestId())).thenReturn(CompletableFuture.completedFuture(1));
        CompletableFuture<Integer> resultFuture = cleanerService.cleanup(dto);
        assertEquals(1, resultFuture.join());
        verify(caseClosureService).closeCase(dto);
        verify(documentDao).deleteByRequestId(dto.getRequestId());
        verify(bankGuaranteeRequestDao).deleteByRequestId(dto.getRequestId());
    }

    @Test
    void cleanupCaseClosureFails() {
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        when(caseClosureService.closeCase(bankGuaranteeRequest)).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Documentum error")));
        CompletableFuture<Integer> resultFuture = cleanerService.cleanup(bankGuaranteeRequest);
        assertEquals(0, resultFuture.join());
        verify(caseClosureService).closeCase(bankGuaranteeRequest);
        verifyNoInteractions(documentDao);
        verifyNoInteractions(bankGuaranteeRequestDao);
    }
}
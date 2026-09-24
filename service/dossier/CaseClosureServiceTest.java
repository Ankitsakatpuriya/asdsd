package com.ing.bankguarantees.service.dossier;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import com.ing.bankguarantees.service.dossier.CaseClosureService;
import com.ing.bankguarantees.service.dossier.DossierSetupService;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;


@ExtendWith(MockitoExtension.class)
public class CaseClosureServiceTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String UPDATE_REQUEST_DOSSIER_RESPONSE_ID = "344135tplov";

    @Mock
    private DossierSetupService dossierSetupService;

    private BankGuaranteeRequestData bankGuaranteeRequestData;

    @InjectMocks
    private CaseClosureService caseClosureService;

    @BeforeEach
    void setup() {
        bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
    }

    @ParameterizedTest
    @MethodSource("bankGuaranteeStatuses")
    void dossierCaseClosePositive(BankGuaranteeRequestStatus bgStatus) {
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        bankGuaranteeRequest.setStatus(bgStatus);
        lenient().when(dossierSetupService.closeCaseInDocumentum(any(), any())).thenReturn(CompletableFuture.completedFuture(UPDATE_REQUEST_DOSSIER_RESPONSE_ID));
        lenient().when(dossierSetupService.closeCaseRequestDossier(any())).thenReturn(CompletableFuture.completedFuture(UPDATE_REQUEST_DOSSIER_RESPONSE_ID));
        String result = caseClosureService.closeCase(bankGuaranteeRequest).join();

        checkResult(result, bgStatus);
        assertThat(result).isNotNull();

    }

    private void checkResult(String result, BankGuaranteeRequestStatus bgStatus) {
        assertThat(result).isNotNull();

        switch (bgStatus) {
            case DRAFT:
                assertThat(result).isBlank();
                break;
            case CREATED, ERROR, CANCELED, FULFILLED:
                assertThat(result).isEqualTo(UPDATE_REQUEST_DOSSIER_RESPONSE_ID);
                break;
        }
    }


    private static Stream<BankGuaranteeRequestStatus> bankGuaranteeStatuses() {
        return Stream.of(
                BankGuaranteeRequestStatus.DRAFT, BankGuaranteeRequestStatus.CREATED,
                BankGuaranteeRequestStatus.ERROR, BankGuaranteeRequestStatus.CANCELED,
                BankGuaranteeRequestStatus.FULFILLED
        );
    }
}

package com.ing.bankguarantees.service.documentsigning;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.dar.model.request.DarInput;
import com.ing.bankguarantees.remote.rest.dar.model.response.DarListResponse;
import com.ing.bankguarantees.remote.rest.dar.model.response.DarListResponse.DarResponse;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DarDigitalSignatureServiceTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String DOCUMENT_ID = "344135tplow";
    private static final String DAR_UUID = "b59f13b6-0fee-468a-8c7f-c1df1a2b5453";

    @Mock
    private ClientGateway<DarInput, Optional<DarResponse>, DarListResponse> docSignGateway;

    @InjectMocks
    private DarDigitalSignatureService darDigitalSignatureService;

    @Test
    void checkGetDarResponsePositive() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        DarInput darInput = MockHelper.getDarInput(bankGuaranteeRequestData, List.of(document));
        DarResponse darResponse = MockHelper.getDarResponse(DAR_UUID);
        given(docSignGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(Optional.of(darResponse)));
        DarResponse result = darDigitalSignatureService.getDarResponse(darInput).join();
        assertThat(result).isEqualTo(darResponse);
    }

    @Test
    void checkGetEmptyDarResponse() {

        given(docSignGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(Optional.empty()));
        CompletableFuture<DarResponse> future = darDigitalSignatureService.getDarResponse(null);
        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }


}

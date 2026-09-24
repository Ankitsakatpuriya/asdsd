package com.ing.bankguarantees.service.stp;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.domain.StpResultDataSet.STPResultData;
import com.ing.bankguarantees.models.enums.StpCriteriaType;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.pamqualification.model.response.PamQualificationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PamQualificationCheckServiceTest {

    private static final String LEGAL_ENTITY_ID = "b6c4e28c-8a3a-4e58-8063-b86c2f3e84d8";
    private static final String INSTRUCTING_PARTY_PASS_JUSTIFICATION = "Instructing Party cdd pass";
    private static final String INSTRUCTING_PARTY_FAILED_JUSTIFICATION = "Instructing Party cdd failed";


    @Mock
    private ClientGateway<String, Boolean, PamQualificationResponse> pamQualificationGateway;

    @InjectMocks
    private PamQualificationCheckService pamQualificationCheckService;

    @Test
    void checkInstructingPartyCddStatusPositive() {
        given(pamQualificationGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(true));
        STPResultData stpResultData = pamQualificationCheckService.checkInstructingPartyCddStatus(LEGAL_ENTITY_ID).join();
        assertThat(stpResultData).isNotNull();
        assertThat(stpResultData.isStpPossible()).isTrue();
        assertThat(stpResultData.getType()).isEqualTo(StpCriteriaType.INSTRUCTING_PARTY_CDD);
        assertThat(stpResultData.getJustification()).isEqualTo(INSTRUCTING_PARTY_PASS_JUSTIFICATION);
    }

    @Test
    void checkInstructingPartyCddStatusFailed() {
        given(pamQualificationGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(false));
        STPResultData stpResultData = pamQualificationCheckService.checkInstructingPartyCddStatus(LEGAL_ENTITY_ID).join();
        assertThat(stpResultData).isNotNull();
        assertThat(stpResultData.isStpPossible()).isFalse();
        assertThat(stpResultData.getType()).isEqualTo(StpCriteriaType.INSTRUCTING_PARTY_CDD);
        assertThat(stpResultData.getJustification()).isEqualTo(INSTRUCTING_PARTY_FAILED_JUSTIFICATION);

    }

    @Test
    void checkInstructingPartyCddStatusError() {

        given(pamQualificationGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<STPResultData> stpResultFuture = pamQualificationCheckService.checkInstructingPartyCddStatus(LEGAL_ENTITY_ID);
        CompletionException exception = assertThrows(CompletionException.class, stpResultFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }
}

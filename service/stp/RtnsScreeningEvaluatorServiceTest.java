package com.ing.bankguarantees.service.stp;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.namescreening.model.request.NameScreeningInput;
import com.ing.bankguarantees.remote.rest.namescreening.model.response.NameScreeningResponse;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RtnsScreeningEvaluatorServiceTest {

    @Mock
    private ClientGateway<NameScreeningInput, NameScreeningResponse, NameScreeningResponse> nameScreeningClientGateway;

    @InjectMocks
    private RtnsScreeningEvaluatorService rtnsScreeningEvaluatorService;


    @Test
    void checkNameScreeningPositive() {
        NameScreeningInput nameScreeningInput = MockHelper.getNameScreeningInput();
        NameScreeningResponse nameScreeningResponse = MockHelper.getNameScreeningResponse(0);
        given(nameScreeningClientGateway.performRequestWithValidate(nameScreeningInput)).willReturn(CompletableFuture.completedFuture(nameScreeningResponse));
        Boolean status = rtnsScreeningEvaluatorService.checkNameScreening(nameScreeningInput).join();
        assertThat(status).isNotNull().isTrue();
    }

    @Test
    void checkNameScreeningNegative() {
        NameScreeningInput nameScreeningInput = MockHelper.getNameScreeningInput();
        NameScreeningResponse nameScreeningResponse = MockHelper.getNameScreeningResponse(1);
        given(nameScreeningClientGateway.performRequestWithValidate(nameScreeningInput)).willReturn(CompletableFuture.completedFuture(nameScreeningResponse));
        Boolean status = rtnsScreeningEvaluatorService.checkNameScreening(nameScreeningInput).join();
        assertThat(status).isNotNull().isFalse();
    }

    @Test
    void checkNameScreeningError() {
        NameScreeningInput nameScreeningInput = MockHelper.getNameScreeningInput();
        given(nameScreeningClientGateway.performRequestWithValidate(nameScreeningInput)).willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<Boolean> booleanCompletableFuture = rtnsScreeningEvaluatorService.checkNameScreening(nameScreeningInput);
        CompletionException exception = assertThrows(CompletionException.class, booleanCompletableFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }
}


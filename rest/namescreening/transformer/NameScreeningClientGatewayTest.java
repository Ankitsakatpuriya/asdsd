package com.ing.bankguarantees.remote.rest.namescreening.transformer;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.rest.namescreening.model.request.NameScreeningInput;
import com.ing.bankguarantees.remote.rest.namescreening.model.response.NameScreeningResponse;
import com.ing.bankguarantees.util.MockHelper;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class NameScreeningClientGatewayTest {
    @Mock
    private Request request;

    @Mock
    private JavaService<Request, NameScreeningResponse> restClient;

    @Mock
    private ResponseValidator<NameScreeningResponse> responseValidator;

    @Mock
    private NameScreeningReqTransformer requestTransformer;

    private ClientGateway<NameScreeningInput, NameScreeningResponse, NameScreeningResponse> nameScreeningClientGateway;

    @BeforeEach
    void init() {
        nameScreeningClientGateway = new ClientGateway<>(restClient, requestTransformer::transform,
                responseValidator::validate, ExecutorConfig.workStealingPool());
    }

    @Test
    void getNameScreeningWithAllAnswers() {


        NameScreeningResponse nameScreeningResponse = MockHelper.getNameScreeningResponse(1);
        NameScreeningInput nameScreeningInput = MockHelper.getNameScreeningInput();
        given(restClient.apply(any(Request.class))).willReturn(completedFuture(nameScreeningResponse));
        given(responseValidator.validate(nameScreeningResponse)).willReturn(nameScreeningResponse);
        given(requestTransformer.transform(nameScreeningInput)).willReturn(request);

        CompletableFuture<NameScreeningResponse> nameScreeningResponseCompletableFuture =
                nameScreeningClientGateway.performRequestWithValidate(nameScreeningInput);
        assertThat(nameScreeningResponseCompletableFuture.join()).isNotNull().isEqualTo(nameScreeningResponse);
    }

    @ParameterizedTest
    @ValueSource(strings = {"errors/InternalServerErrorItem.json", "errors/BadRequest.json", "errors/NotFoundErrorItem.json"})
    void checkForClientException(String filename) {

        NameScreeningInput nameScreeningInput = MockHelper.getNameScreeningInput();
        CompletableFuture<NameScreeningResponse> badFuture = new CompletableFuture<>();
        ErrorItem errorItem = MockHelper.CreateErrorItem((filename));
        ClientException clientException = new ClientException(errorItem.getCode(), ErrorSource.NSA, errorItem);
        badFuture.completeExceptionally(clientException);
        given(restClient.apply(any(Request.class))).willReturn(badFuture);
        given(requestTransformer.transform(any(NameScreeningInput.class))).willReturn(request);

        CompletableFuture<NameScreeningResponse> nameScreeningResponseCompletableFuture =
                nameScreeningClientGateway.performRequestWithValidate(nameScreeningInput);
        Exception exception = assertThrows(ExecutionException.class, nameScreeningResponseCompletableFuture::get);
        assertThat(errorItem.getCode()).isEqualTo(((ClientException) exception.getCause()).getErrorItem().getCode());

    }

    @Test
    void checkForEmptyResponse() {

        NameScreeningInput nameScreeningInput = MockHelper.getNameScreeningInput();
        CompletableFuture<NameScreeningResponse> failingFuture = new CompletableFuture<>();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        failingFuture.completeExceptionally(bgosException);
        //GIVEN
        given(restClient.apply(any(Request.class))).willReturn(failingFuture);
        given(requestTransformer.transform(any(NameScreeningInput.class))).willReturn(request);

        CompletableFuture<NameScreeningResponse> nameScreeningResponseCompletableFuture =
                nameScreeningClientGateway.performRequestWithValidate(nameScreeningInput);
        Exception exception = assertThrows(ExecutionException.class, nameScreeningResponseCompletableFuture::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

    @Test
    void checkForConstraintValidation() {
        NameScreeningResponse nameScreeningResponse = MockHelper.getNameScreeningResponse(0);
        NameScreeningInput nameScreeningInput = MockHelper.getNameScreeningInput();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        given(responseValidator.validate(nameScreeningResponse)).willThrow(bgosException);

        given(restClient.apply(any(Request.class))).willReturn(completedFuture(nameScreeningResponse));
        given(requestTransformer.transform(any(NameScreeningInput.class))).willReturn(request);
        CompletableFuture<NameScreeningResponse> nameScreeningResponseCompletableFuture =
                nameScreeningClientGateway.performRequestWithValidate(nameScreeningInput);
        Exception exception = assertThrows(ExecutionException.class, nameScreeningResponseCompletableFuture::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

}

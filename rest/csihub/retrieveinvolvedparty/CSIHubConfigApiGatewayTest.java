package com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.request.CSIHubInvolvedPartiesRequest;
import com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.response.InvolvedPartiesCsiHubResponse;
import com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.transformer.InvolvedPartyCsiReqTransformer;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.remote.utils.RequestAdapter;
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
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CSIHubConfigApiGatewayTest {
    @Mock
    private Request request;
    @Mock
    JavaService<Request, InvolvedPartiesCsiHubResponse> restClient;
    @Mock
    ResponseValidator<InvolvedPartiesCsiHubResponse> responseValidator;

    @Mock
    private InvolvedPartyCsiReqTransformer requestTransformer;

    private ClientGateway<CSIHubInvolvedPartiesRequest, InvolvedPartiesCsiHubResponse, InvolvedPartiesCsiHubResponse> csiInvolvePartyGateway;

    @BeforeEach
    void init() {
        csiInvolvePartyGateway = new ClientGateway<>(restClient, requestTransformer::transform,
                responseValidator::validate, ExecutorConfig.workStealingPool());
    }

    @Test
    void getConnectDotWithAllAnswers() {
        CSIHubInvolvedPartiesRequest csiHubInvolvedPartiesRequest = RequestAdapter.getCsiCbeRequest("123");
        InvolvedPartiesCsiHubResponse involvedPartiesCsiHubResponse = MockHelper.getInvolvedPartiesCsiHubResponse();
        given(restClient.apply(any(Request.class))).willReturn(completedFuture(involvedPartiesCsiHubResponse));
        given(responseValidator.validate(involvedPartiesCsiHubResponse)).willReturn(involvedPartiesCsiHubResponse);
        given(requestTransformer.transform(csiHubInvolvedPartiesRequest)).willReturn(request);

        CompletableFuture<InvolvedPartiesCsiHubResponse> connectDotBoolean = csiInvolvePartyGateway.performRequestWithValidate(csiHubInvolvedPartiesRequest);
        assertThat(connectDotBoolean.join()).isNotNull().isEqualTo(involvedPartiesCsiHubResponse);

    }

    @ParameterizedTest
    @ValueSource(strings = {"errors/InternalServerErrorItem.json", "errors/BadRequest.json", "errors/NotFoundErrorItem.json"})
    void checkForClientException(String filename) {

        CSIHubInvolvedPartiesRequest csiHubInvolvedPartiesRequest = RequestAdapter.getCsiCbeRequest("123");
        CompletableFuture<InvolvedPartiesCsiHubResponse> badFuture = new CompletableFuture<>();
        ErrorItem errorItem = MockHelper.CreateErrorItem((filename));
        ClientException clientException = new ClientException(errorItem.getCode(), ErrorSource.CHA, errorItem);
        badFuture.completeExceptionally(clientException);
        given(restClient.apply(any(Request.class))).willReturn(badFuture);
        given(requestTransformer.transform(csiHubInvolvedPartiesRequest)).willReturn(request);

        CompletableFuture<InvolvedPartiesCsiHubResponse> expectedOutput =
                csiInvolvePartyGateway.performRequestWithValidate(csiHubInvolvedPartiesRequest);
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(errorItem.getCode()).isEqualTo(((ClientException) exception.getCause()).getErrorItem().getCode());

    }
    @Test
    void checkForEmptyResponse() {
        CSIHubInvolvedPartiesRequest csiHubInvolvedPartiesRequest = RequestAdapter.getCsiCbeRequest("123");
        CompletableFuture<InvolvedPartiesCsiHubResponse> failingFuture = new CompletableFuture<>();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        failingFuture.completeExceptionally(bgosException);
        given(restClient.apply(any(Request.class))).willReturn(failingFuture);
        given(requestTransformer.transform(csiHubInvolvedPartiesRequest)).willReturn(request);

        CompletableFuture<InvolvedPartiesCsiHubResponse> expectedOutput =
                csiInvolvePartyGateway.performRequestWithValidate(csiHubInvolvedPartiesRequest);
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

    @Test
    void checkForConstraintValidation() {

        CSIHubInvolvedPartiesRequest csiHubInvolvedPartiesRequest = RequestAdapter.getCsiCbeRequest("123");
        InvolvedPartiesCsiHubResponse involvedPartiesCsiHubResponse = MockHelper.getInvolvedPartiesCsiHubResponse();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);

        given(restClient.apply(any(Request.class))).willReturn(completedFuture(involvedPartiesCsiHubResponse));
        given(requestTransformer.transform(csiHubInvolvedPartiesRequest)).willReturn(request);
        given(responseValidator.validate(involvedPartiesCsiHubResponse)).willThrow(bgosException);

        CompletableFuture<InvolvedPartiesCsiHubResponse> expectedOutput =
                csiInvolvePartyGateway.performRequestWithValidate(csiHubInvolvedPartiesRequest);

        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }
}

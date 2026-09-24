package com.ing.bankguarantees.remote.rest.pamqualification;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.rest.pamqualification.model.response.PamQualificationResponse;
import com.ing.bankguarantees.remote.rest.pamqualification.transformer.PamQualificationReqTransformer;
import com.ing.bankguarantees.remote.rest.pamqualification.transformer.PamQualificationResTransformer;
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
class PamQualificationGatewayTest {

    private static final String ORGANIZATION_ID = "b6c4e28c-8a3a-4e58-8063-b86c2f3e84d8";
    private static final String INDIVIDUAL_ID = "e9b5fcdc-7f29-4c81-b804-8ee06603c0c7";


    @Mock
    private Request request;

    @Mock
    private JavaService<Request, PamQualificationResponse> restClient;

    @Mock
    private ResponseValidator<PamQualificationResponse> responseValidator;

    @Mock
    private PamQualificationResTransformer responseTransformer;

    @Mock
    private PamQualificationReqTransformer requestTransformer;

    private ClientGateway<String, Boolean, PamQualificationResponse> pamQualificationResponseClientGateway;

    @BeforeEach
    void init() {
        pamQualificationResponseClientGateway = new ClientGateway<>(restClient, requestTransformer::transform, (res, in) -> responseTransformer.transform(res), responseValidator::validate, ExecutorConfig.workStealingPool());
    }

    @Test
    void getPamQualification() {

        PamQualificationResponse pamQualificationResponse = MockHelper.getPamQualificationResponse();
        given(restClient.apply(any(Request.class))).willReturn(completedFuture(pamQualificationResponse));
        given(responseValidator.validate(pamQualificationResponse)).willReturn(pamQualificationResponse);
        given(requestTransformer.transform(any())).willReturn(request);
        given(responseTransformer.transform(pamQualificationResponse)).willReturn(true);
        CompletableFuture<Boolean> actualOrgInvolveParty = pamQualificationResponseClientGateway.performRequest(ORGANIZATION_ID);
        assertThat(actualOrgInvolveParty.join()).isNotNull().isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"errors/InternalServerErrorItem.json", "errors/BadRequest.json", "errors/NotFoundErrorItem.json"})
    void checkForClientException(String filename) {
        CompletableFuture<PamQualificationResponse> badFuture = new CompletableFuture<>();
        ErrorItem errorItem = MockHelper.CreateErrorItem((filename));
        ClientException clientException = new ClientException(errorItem.getCode(), ErrorSource.PQC, errorItem);
        badFuture.completeExceptionally(clientException);
        given(restClient.apply(any(Request.class))).willReturn(badFuture);
        given(requestTransformer.transform(any())).willReturn(request);

        CompletableFuture<Boolean> actualOrgInvolveParty = pamQualificationResponseClientGateway.performRequest(ORGANIZATION_ID);
        Exception exception = assertThrows(ExecutionException.class, actualOrgInvolveParty::get);
        assertThat(errorItem.getCode()).isEqualTo(((ClientException) exception.getCause()).getErrorItem().getCode());

    }

    @Test
    void checkForEmptyResponse() {
        CompletableFuture<PamQualificationResponse> failingFuture = new CompletableFuture<>();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        failingFuture.completeExceptionally(bgosException);
        given(requestTransformer.transform(any())).willReturn(request);
        given(restClient.apply(any(Request.class))).willReturn(failingFuture);

        CompletableFuture<Boolean> actualOrgInvolveParty =
                pamQualificationResponseClientGateway.performRequest(INDIVIDUAL_ID);
        Exception exception = assertThrows(ExecutionException.class, actualOrgInvolveParty::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

    @Test
    void checkForConstraintValidation() {

        PamQualificationResponse pamQualificationResponse = MockHelper.getPamQualificationResponse();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        given(restClient.apply(any(Request.class))).willReturn(completedFuture(pamQualificationResponse));
        given(requestTransformer.transform(any())).willReturn(request);
        given(responseValidator.validate(pamQualificationResponse)).willThrow(bgosException);
        CompletableFuture<Boolean> actualOrgInvolveParty =pamQualificationResponseClientGateway.performRequest(INDIVIDUAL_ID);
        Exception exception = assertThrows(ExecutionException.class, actualOrgInvolveParty::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

}

package com.ing.bankguarantees.remote.rest.involveparty.externalidentifier;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.model.response.ExternalIdentifierListResponse;
import com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.transformer.ExtIdentReqTransformer;
import com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.transformer.ExtIdentRespTransformer;
import com.ing.bankguarantees.util.MockHelper;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ExternalIdentifierClientGatewayTest {

    @Mock
    private Request request;

    @Mock
    private JavaService<Request, ExternalIdentifierListResponse> restClient;

    @Mock
    private ResponseValidator<ExternalIdentifierListResponse> responseValidator;

    @Mock
    private ExtIdentReqTransformer requestTransformer;

    @Mock
    private ExtIdentRespTransformer responseTransformer;

    private static final String KBO_BE = "KBO_BE";

    private ClientGateway<String, Optional<String>, ExternalIdentifierListResponse> externalIdentifierClientGateway;

    @BeforeEach
    void init() {
        externalIdentifierClientGateway = new ClientGateway<>(restClient, requestTransformer::transform,(res, in) -> responseTransformer.transform(res),
                responseValidator::validate, ExecutorConfig.workStealingPool());
    }

    @Test
    void getExternalIdentifierForOrgId() {

        ExternalIdentifierListResponse externalIdentifierListResponse = MockHelper.getExternalIdentifierResponse(
                KBO_BE, KBO_BE);
        given(restClient.apply(any(Request.class))).willReturn(completedFuture(externalIdentifierListResponse));
        given(responseValidator.validate(externalIdentifierListResponse)).willReturn(externalIdentifierListResponse);
        given(requestTransformer.transform(anyString())).willReturn(request);
        given(responseTransformer.transform(externalIdentifierListResponse)).willReturn(Optional.of(KBO_BE));

        CompletableFuture<Optional<String>> actualOrgInvolveParty = externalIdentifierClientGateway.performRequest(anyString());

        Assertions.assertNotNull(actualOrgInvolveParty.join());
    }

    @ParameterizedTest
    @ValueSource(strings = {"errors/InternalServerErrorItem.json", "errors/BadRequest.json", "errors/NotFoundErrorItem.json"})
    void checkForClientException(String filename) {

        CompletableFuture<ExternalIdentifierListResponse> badFuture = new CompletableFuture<>();
        ErrorItem errorItem = MockHelper.CreateErrorItem((filename));
        ClientException clientException = new ClientException(errorItem.getCode(), ErrorSource.IPA, errorItem);
        badFuture.completeExceptionally(clientException);
        given(restClient.apply(any(Request.class))).willReturn(badFuture);
        given(requestTransformer.transform(anyString())).willReturn(request);

        CompletableFuture<Optional<String>> actualOrgInvolveParty = externalIdentifierClientGateway.performRequest(anyString());

        Exception exception = assertThrows(ExecutionException.class, actualOrgInvolveParty::get);
        assertThat(errorItem.getCode()).isEqualTo(((ClientException) exception.getCause()).getErrorItem().getCode());

    }

    @Test
    void checkForEmptyResponse() {

        CompletableFuture<ExternalIdentifierListResponse> failingFuture = new CompletableFuture<>();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        failingFuture.completeExceptionally(bgosException);
        given(restClient.apply(any(Request.class))).willReturn(failingFuture);
        given(requestTransformer.transform(anyString())).willReturn(request);

        CompletableFuture<Optional<String>> actualOrgInvolveParty = externalIdentifierClientGateway.performRequest(anyString());

        Exception exception = assertThrows(ExecutionException.class, actualOrgInvolveParty::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

    @Test
    void checkForConstraintValidation() {

        ExternalIdentifierListResponse externalIdentifierListResponse = MockHelper.getExternalIdentifierResponse(
                KBO_BE, KBO_BE);
        externalIdentifierListResponse.setInvolvedPartyExternalIdentifiers(null);
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        given(restClient.apply(any(Request.class))).willReturn(completedFuture(externalIdentifierListResponse));
        given(requestTransformer.transform(anyString())).willReturn(request);
        given(responseValidator.validate(externalIdentifierListResponse)).willThrow(bgosException);

        CompletableFuture<Optional<String>> actualOrgInvolveParty = externalIdentifierClientGateway.performRequest(anyString());

        Exception exception = assertThrows(ExecutionException.class, actualOrgInvolveParty::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

}

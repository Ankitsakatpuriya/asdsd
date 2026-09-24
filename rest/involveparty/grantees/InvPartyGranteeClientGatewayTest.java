package com.ing.bankguarantees.remote.rest.involveparty.grantees;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.rest.involveparty.grantees.model.response.GranteeGrantorResponse;
import com.ing.bankguarantees.remote.rest.involveparty.grantees.transformer.InvPartyGranteeReqTransformer;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class InvPartyGranteeClientGatewayTest {

    @Mock
    private Request request;

    @Mock
    private JavaService<Request, GranteeGrantorResponse> restClient;

    @Mock
    private ResponseValidator<GranteeGrantorResponse> responseValidator;

    @Mock
    private InvPartyGranteeReqTransformer requestTransformer;


    private ClientGateway<String, GranteeGrantorResponse, GranteeGrantorResponse> involvePartyGranteeClientGateway;

    @BeforeEach
    void init() {
        involvePartyGranteeClientGateway = new ClientGateway<>(restClient, requestTransformer::transform, responseValidator::validate, ExecutorConfig.workStealingPool());
    }

    @Test
    void getInvolvePartyGranteeForOrgId() {

        GranteeGrantorResponse granteeGrantorResponse = MockHelper.getGranteeGrantorResponse();
        given(restClient.apply(any(Request.class))).willReturn(completedFuture(granteeGrantorResponse));
        given(responseValidator.validate(granteeGrantorResponse)).willReturn(granteeGrantorResponse);
        given(requestTransformer.transform(anyString())).willReturn(request);

        CompletableFuture<GranteeGrantorResponse> granteeGrantorResponseCompletableFuture
                = involvePartyGranteeClientGateway.performRequestWithValidate(anyString());

        assertNotNull(granteeGrantorResponseCompletableFuture.join());
    }

    @ParameterizedTest
    @ValueSource(strings = {"errors/InternalServerErrorItem.json", "errors/BadRequest.json", "errors/NotFoundErrorItem.json"})
    void checkForClientException(String filename) {

        CompletableFuture<GranteeGrantorResponse> badFuture = new CompletableFuture<>();
        ErrorItem errorItem = MockHelper.CreateErrorItem((filename));
        ClientException clientException = new ClientException(errorItem.getCode(), ErrorSource.IPA, errorItem);
        badFuture.completeExceptionally(clientException);
        given(restClient.apply(any(Request.class))).willReturn(badFuture);
        given(requestTransformer.transform(anyString())).willReturn(request);

        CompletableFuture<GranteeGrantorResponse> granteeGrantorResponseCompletableFuture
                = involvePartyGranteeClientGateway.performRequestWithValidate(anyString());
        Exception exception = assertThrows(ExecutionException.class, granteeGrantorResponseCompletableFuture::get);
        assertThat(errorItem.getCode()).isEqualTo(((ClientException) exception.getCause()).getErrorItem().getCode());

    }

    @Test
    void checkForEmptyResponse() {

        CompletableFuture<GranteeGrantorResponse> failingFuture = new CompletableFuture<>();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        failingFuture.completeExceptionally(bgosException);
        given(restClient.apply(any(Request.class))).willReturn(failingFuture);
        given(requestTransformer.transform(anyString())).willReturn(request);

        CompletableFuture<GranteeGrantorResponse> granteeGrantorResponseCompletableFuture
                = involvePartyGranteeClientGateway.performRequestWithValidate(anyString());
        Exception exception = assertThrows(ExecutionException.class, granteeGrantorResponseCompletableFuture::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

    @Test
    void checkForConstraintValidation() {

        GranteeGrantorResponse granteeGrantorResponse = MockHelper.getGranteeGrantorResponse();
        granteeGrantorResponse.setInvolvedPartyInvolvedPartyRelationships(null);
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        given(restClient.apply(any(Request.class))).willReturn(completedFuture(granteeGrantorResponse));
        given(requestTransformer.transform(anyString())).willReturn(request);
        given(responseValidator.validate(granteeGrantorResponse)).willThrow(bgosException);

        CompletableFuture<GranteeGrantorResponse> granteeGrantorResponseCompletableFuture
                = involvePartyGranteeClientGateway.performRequestWithValidate(anyString());

        Exception exception = assertThrows(ExecutionException.class, granteeGrantorResponseCompletableFuture::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

}

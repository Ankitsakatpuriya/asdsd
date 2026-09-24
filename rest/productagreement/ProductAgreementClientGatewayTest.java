package com.ing.bankguarantees.remote.rest.productagreement;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementAccount;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementResponse;
import com.ing.bankguarantees.remote.rest.productagreement.transformer.ProductAgreReqTransformer;
import com.ing.bankguarantees.remote.rest.productagreement.transformer.ProductAgreeRespTransformer;
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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ProductAgreementClientGatewayTest {
    @Mock
    private Request request;

    @Mock
    private JavaService<Request, ProductAgreementResponse> restClient;

    @Mock
    private ResponseValidator<ProductAgreementResponse> responseValidator;

    @Mock
    private ProductAgreementProperties properties;

    @Mock
    private ProductAgreReqTransformer requestTransformer;

    @Mock
    private ProductAgreeRespTransformer responseTransformer;


    private ClientGateway<String, List<ProductAgreementAccount>, ProductAgreementResponse> productAgreementResponseClientGateway;

    @BeforeEach
    void init() {
        productAgreementResponseClientGateway = new ClientGateway<>(restClient, requestTransformer::transform,(res, in) -> responseTransformer.transform(any(), anyString()),
                responseValidator::validate, ExecutorConfig.workStealingPool());
    }

    @Test
    void getExternalIdentifierForOrgId() {

        var productAgreementAccount = MockHelper.getProductAgreementAccount();
        var productAgreements = MockHelper.createProductAgreements("BGA/FDA/ProductAgreements.json");
        given(restClient.apply(any(Request.class))).willReturn(completedFuture(productAgreements));
        given(responseValidator.validate(productAgreements)).willReturn(productAgreements);
        given(requestTransformer.transform(anyString())).willReturn(request);
        given(responseTransformer.transform(any(), anyString())).willReturn(productAgreementAccount);

        CompletableFuture<List<ProductAgreementAccount>> actualOrgInvolveParty = productAgreementResponseClientGateway
                .performRequest(anyString());

        assertThat(actualOrgInvolveParty.join()).isNotNull().isEqualTo(productAgreementAccount);

    }
    @ParameterizedTest
    @ValueSource(strings = {"errors/InternalServerErrorItem.json", "errors/BadRequest.json", "errors/NotFoundErrorItem.json"})
    void checkForClientException(String filename) {

        CompletableFuture<ProductAgreementResponse> badFuture = new CompletableFuture<>();
        ErrorItem errorItem = MockHelper.CreateErrorItem((filename));
        ClientException clientException = new ClientException(errorItem.getCode(), ErrorSource.PAA, errorItem);
        badFuture.completeExceptionally(clientException);
        given(restClient.apply(any(Request.class))).willReturn(badFuture);
        given(requestTransformer.transform(anyString())).willReturn(request);

        CompletableFuture<List<ProductAgreementAccount>> expectedOutput = productAgreementResponseClientGateway.
                performRequest(anyString());
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(errorItem.getCode()).isEqualTo(((ClientException) exception.getCause()).getErrorItem().getCode());

    }

    @Test
    void checkForEmptyResponse() {

        CompletableFuture<ProductAgreementResponse> failingFuture = new CompletableFuture<>();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        failingFuture.completeExceptionally(bgosException);
        given(restClient.apply(any(Request.class))).willReturn(failingFuture);
        given(requestTransformer.transform(anyString())).willReturn(request);

        CompletableFuture<List<ProductAgreementAccount>> expectedOutput = productAgreementResponseClientGateway.
                performRequest(anyString());
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

    @Test
    void checkForConstraintValidation() {

        var productAgreements = MockHelper.createProductAgreements("BGA/FDA/ProductAgreements.json");
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);

        given(restClient.apply(any(Request.class))).willReturn(completedFuture(productAgreements));
        given(requestTransformer.transform(anyString())).willReturn(request);
        given(responseValidator.validate(productAgreements)).willThrow(bgosException);

        CompletableFuture<List<ProductAgreementAccount>> expectedOutput = productAgreementResponseClientGateway.
                performRequest(anyString());
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

}

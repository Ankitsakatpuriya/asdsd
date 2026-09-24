package com.ing.bankguarantees.remote.rest.creditlinebalance;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.request.CreditBalanceInput;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.response.CreditBalanceOutput;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.response.CreditLineBalanceResponse;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.transformer.CreditLineReqTransformer;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.transformer.CreditLineResTransformer;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementAccount;
import com.ing.bankguarantees.remote.utils.RequestAdapter;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CreditLineClientGatewayTest {

    @Mock
    private Request request;

    @Mock
    private JavaService<Request, CreditLineBalanceResponse> restClient;

    @Mock
    private ResponseValidator<CreditLineBalanceResponse> responseValidator;

    @Mock(name = "requestTransformer")
    private CreditLineReqTransformer requestTransformer;

    @Mock(name = "responseTransformer")
    private CreditLineResTransformer responseTransformer;

    private ClientGateway<CreditBalanceInput, List<CreditBalanceOutput>, CreditLineBalanceResponse> creditLineClientGateway;

    @BeforeEach
    void init() {
        creditLineClientGateway = new ClientGateway<>(restClient, requestTransformer::transform, responseTransformer::transform,
                responseValidator::validate, ExecutorConfig.workStealingPool());
    }

    @Test
    void getAccountBalancesWithAllAnswers() {


        CreditLineBalanceResponse creditLineResponse = MockHelper.getCreditLineResponse();
        ProductAgreementAccount productAgreement = MockHelper.getCreditLineProductAgreement();
        CreditBalanceInput creditLineInput = RequestAdapter.getCreditLineRequest(productAgreement);
        List<CreditBalanceOutput> creditBalanceOutput = MockHelper.getCreditBalanceOutput(creditLineResponse, creditLineInput);


        given(restClient.apply(any(Request.class))).willReturn(completedFuture(creditLineResponse));
        given(responseValidator.validate(creditLineResponse)).willReturn(creditLineResponse);
        given(requestTransformer.transform(creditLineInput)).willReturn(request);
        given(responseTransformer.transform(creditLineResponse, creditLineInput)).willReturn(creditBalanceOutput);

        CompletableFuture<List<CreditBalanceOutput>> actualAccountList = creditLineClientGateway.performRequest(creditLineInput);
        assertThat(actualAccountList.join()).isNotNull().isEqualTo(creditBalanceOutput);
    }

    @ParameterizedTest
    @ValueSource(strings = {"errors/InternalServerErrorItem.json", "errors/BadRequest.json", "errors/NotFoundErrorItem.json"})
    void checkForClientException(String filename) {

        ProductAgreementAccount productAgreement = MockHelper.getCreditLineProductAgreement();
        CreditBalanceInput creditLineInput = RequestAdapter.getCreditLineRequest(productAgreement);
        CompletableFuture<CreditLineBalanceResponse> badFuture = new CompletableFuture<>();
        ErrorItem errorItem = MockHelper.CreateErrorItem((filename));
        ClientException clientException = new ClientException(errorItem.getCode(), ErrorSource.CLAA, errorItem);
        badFuture.completeExceptionally(clientException);
        given(restClient.apply(any(Request.class))).willReturn(badFuture);
        given(requestTransformer.transform(any(CreditBalanceInput.class))).willReturn(request);

        CompletableFuture<List<CreditBalanceOutput>> expectedOutput = creditLineClientGateway.performRequest(creditLineInput);
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(errorItem.getCode()).isEqualTo(((ClientException) exception.getCause()).getErrorItem().getCode());

    }

    @Test
    void checkForEmptyResponse() {

        ProductAgreementAccount productAgreement = MockHelper.getCreditLineProductAgreement();
        CompletableFuture<CreditLineBalanceResponse> failingFuture = new CompletableFuture<>();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        failingFuture.completeExceptionally(bgosException);
        //GIVEN
        given(restClient.apply(any(Request.class))).willReturn(failingFuture);
        given(requestTransformer.transform(any(CreditBalanceInput.class))).willReturn(request);

        CompletableFuture<List<CreditBalanceOutput>> expectedOutput = creditLineClientGateway.performRequest(RequestAdapter.getCreditLineRequest(productAgreement));
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

    @Test
    void checkForConstraintValidation() {

        CreditLineBalanceResponse creditLineResponse = MockHelper.getCreditLineResponse();
        ProductAgreementAccount productAgreement = MockHelper.getCreditLineProductAgreement();
        creditLineResponse.setContractDetails(null);
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);

        given(restClient.apply(any(Request.class))).willReturn(completedFuture(creditLineResponse));
        given(requestTransformer.transform(any(CreditBalanceInput.class))).willReturn(request);
        given(responseValidator.validate(creditLineResponse)).willThrow(bgosException);
        CompletableFuture<List<CreditBalanceOutput>> expectedOutput = creditLineClientGateway.performRequest(RequestAdapter.getCreditLineRequest(productAgreement));

        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

}

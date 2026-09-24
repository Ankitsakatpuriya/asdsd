package com.ing.bankguarantees.remote.rest.accountbalance.transformer;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.rest.accountbalance.model.request.AccountBalanceInput;
import com.ing.bankguarantees.remote.rest.accountbalance.model.response.AccountBalanceOutput;
import com.ing.bankguarantees.remote.rest.accountbalance.model.response.AccountBalanceResponse;
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
class AccountBalanceClientGatewayTest {

    @Mock
    private Request request;

    @Mock
    private JavaService<Request, AccountBalanceResponse> restClient;

    @Mock
    private ResponseValidator<AccountBalanceResponse> responseValidator;

    @Mock(name = "requestTransformer")
    private AccountBalanceReqTransformer requestTransformer;

    @Mock(name = "responseTransformer")
    private AccountBalanceResTransformer responseTransformer;

    private ClientGateway<AccountBalanceInput, List<AccountBalanceOutput>, AccountBalanceResponse> acctBalanceGateway;

    @BeforeEach
    void init() {
        acctBalanceGateway = new ClientGateway<>(restClient, requestTransformer::transform, responseTransformer::transform,
                responseValidator::validate, ExecutorConfig.workStealingPool());
    }

    @Test
    void getAccountBalancesWithAllAnswers() {

        AccountBalanceResponse acctBalanceResponse = MockHelper.getAcctBalanceResponse();
        AccountBalanceInput accountBalanceInput = RequestAdapter.getAccountDetailRequest(MockHelper.getAcctBalanceProductAgreement());
        List<AccountBalanceOutput> acctBalanceOutput = MockHelper.getAcctBalanceOutput(acctBalanceResponse, accountBalanceInput);
        ProductAgreementAccount productAgreement = MockHelper.getAcctBalanceProductAgreement();
        given(restClient.apply(any(Request.class))).willReturn(completedFuture(acctBalanceResponse));
        given(responseValidator.validate(acctBalanceResponse)).willReturn(acctBalanceResponse);
        given(requestTransformer.transform(accountBalanceInput)).willReturn(request);
        given(responseTransformer.transform(acctBalanceResponse, accountBalanceInput)).willReturn(acctBalanceOutput);

        CompletableFuture<List<AccountBalanceOutput>> actualAccountList = acctBalanceGateway.performRequest(RequestAdapter.getAccountDetailRequest(productAgreement));
        assertThat(actualAccountList.join()).isNotNull().isEqualTo(acctBalanceOutput);
    }

    @ParameterizedTest
    @ValueSource(strings = {"errors/InternalServerErrorItem.json", "errors/BadRequest.json", "errors/NotFoundErrorItem.json"})
    void checkForClientException(String filename) {

        ProductAgreementAccount productAgreement = MockHelper.getAcctBalanceProductAgreement();
        CompletableFuture<AccountBalanceResponse> badFuture = new CompletableFuture<>();
        ErrorItem errorItem = MockHelper.CreateErrorItem((filename));
        ClientException clientException = new ClientException(errorItem.getCode(), ErrorSource.ABA, errorItem);
        badFuture.completeExceptionally(clientException);
        given(restClient.apply(any(Request.class))).willReturn(badFuture);
        given(requestTransformer.transform(any(AccountBalanceInput.class))).willReturn(request);

        CompletableFuture<List<AccountBalanceOutput>> expectedOutput = acctBalanceGateway.performRequest(RequestAdapter.getAccountDetailRequest(productAgreement));
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(errorItem.getCode()).isEqualTo(((ClientException) exception.getCause()).getErrorItem().getCode());

    }

    @Test
    void checkForEmptyResponse() {

        ProductAgreementAccount productAgreement = MockHelper.getAcctBalanceProductAgreement();
        CompletableFuture<AccountBalanceResponse> failingFuture = new CompletableFuture<>();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        failingFuture.completeExceptionally(bgosException);
        //GIVEN
        given(restClient.apply(any(Request.class))).willReturn(failingFuture);
        given(requestTransformer.transform(any(AccountBalanceInput.class))).willReturn(request);

        CompletableFuture<List<AccountBalanceOutput>> expectedOutput = acctBalanceGateway.performRequest(RequestAdapter.getAccountDetailRequest(productAgreement));
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

    @Test
    void checkForConstraintValidation() {

        AccountBalanceResponse acctBalanceResponse = MockHelper.getAcctBalanceResponse();
        ProductAgreementAccount productAgreement = MockHelper.getAcctBalanceProductAgreement();
        acctBalanceResponse.setAccounts(null);
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);

        given(restClient.apply(any(Request.class))).willReturn(completedFuture(acctBalanceResponse));
        given(requestTransformer.transform(any(AccountBalanceInput.class))).willReturn(request);
        given(responseValidator.validate(acctBalanceResponse)).willThrow(bgosException);
        CompletableFuture<List<AccountBalanceOutput>> expectedOutput = acctBalanceGateway.performRequest(RequestAdapter.getAccountDetailRequest(productAgreement));

        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

}

package com.ing.bankguarantees.remote.rest.amsklc.accountloan;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.rest.amsklc.accountloan.response.AccountLoanResponse;

import com.ing.bankguarantees.remote.rest.amsklc.accountloan.transformer.AccountLoanReqTransformer;
import com.ing.bankguarantees.remote.rest.amsklc.accountloan.transformer.AccountLoanResTransformer;
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

import java.math.BigDecimal;
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
class AccountLoanClientGatewayTest {

    @Mock
    private Request request;

    @Mock
    private JavaService<Request, AccountLoanResponse> restClient;

    @Mock
    private ResponseValidator<AccountLoanResponse> responseValidator;

    @Mock(name = "requestTransformer")
    private AccountLoanReqTransformer requestTransformer;

    @Mock(name = "responseTransformer")
    private AccountLoanResTransformer responseTransformer;

    private ClientGateway<String, Optional<BigDecimal>, AccountLoanResponse> accountLoanResponseClientGateway;

    @BeforeEach
    void init() {
        accountLoanResponseClientGateway = new ClientGateway<>(restClient, requestTransformer::transform, responseTransformer::transform,
                responseValidator::validate, ExecutorConfig.workStealingPool());
    }

    @Test
    void getAccountBalancesWithAllAnswers() {

        AccountLoanResponse accountLoanResponse = MockHelper.getAccountLoanResponse();

        given(restClient.apply(any(Request.class))).willReturn(completedFuture(accountLoanResponse));
        given(responseValidator.validate(accountLoanResponse)).willReturn(accountLoanResponse);
        given(requestTransformer.transform(anyString())).willReturn(request);
        given(responseTransformer.transform(any(), anyString())).willReturn(Optional.of(BigDecimal.valueOf(12345L)));

        var accountLoanResponseFuture = accountLoanResponseClientGateway
                .performRequest("accountNumber");
        assertThat(accountLoanResponseFuture.join()).isNotNull().isEqualTo(Optional.of(BigDecimal.valueOf(12345L)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"errors/InternalServerErrorItem.json", "errors/BadRequest.json", "errors/NotFoundErrorItem.json"})
    void checkForClientException(String filename) {

        CompletableFuture<AccountLoanResponse> badFuture = new CompletableFuture<>();
        ErrorItem errorItem = MockHelper.CreateErrorItem((filename));
        ClientException clientException = new ClientException(errorItem.getCode(), ErrorSource.AKB, errorItem);
        badFuture.completeExceptionally(clientException);
        given(restClient.apply(any(Request.class))).willReturn(badFuture);
        given(requestTransformer.transform(any())).willReturn(request);

        var expectedOutput = accountLoanResponseClientGateway
                .performRequest("contractNumber");
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(errorItem.getCode()).isEqualTo(((ClientException) exception.getCause()).getErrorItem().getCode());

    }

    @Test
    void checkForEmptyResponse() {

        CompletableFuture<AccountLoanResponse> failingFuture = new CompletableFuture<>();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        failingFuture.completeExceptionally(bgosException);
        //GIVEN
        given(restClient.apply(any(Request.class))).willReturn(failingFuture);
        given(requestTransformer.transform(anyString())).willReturn(request);

        var expectedOutput = accountLoanResponseClientGateway.performRequest("contractNumber");
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

    @Test
    void checkForConstraintValidation() {

        AccountLoanResponse accountLoanResponse = MockHelper.getAccountLoanResponse();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);

        given(restClient.apply(any(Request.class))).willReturn(completedFuture(accountLoanResponse));
        given(requestTransformer.transform(anyString())).willReturn(request);
        given(responseValidator.validate(accountLoanResponse)).willThrow(bgosException);
        var expectedOutput = accountLoanResponseClientGateway.performRequest("contractNumber");

        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

}

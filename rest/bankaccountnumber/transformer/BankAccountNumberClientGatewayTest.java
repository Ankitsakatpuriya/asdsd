package com.ing.bankguarantees.remote.rest.bankaccountnumber.transformer;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.rest.bankaccountnumber.model.response.BankAccountNumberResponse;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
@Slf4j
@ExtendWith(MockitoExtension.class)
class BankAccountNumberClientGatewayTest {

    @Mock
    private Request request;

    @Mock
    private JavaService<Request, BankAccountNumberResponse> restClient;

    @Mock
    private ResponseValidator<BankAccountNumberResponse> responseValidator;

    @Mock(name = "requestTransformer")
    private BankAccountNumberReqTransformer requestTransformer;

    @Mock(name = "responseTransformer")
    private BankAccountNumberResTransformer responseTransformer;

    private ClientGateway<String, String, BankAccountNumberResponse> bankAccountNumberResponseClientGateway;

    private static final String ACCOUNT_NUMBER = "accountNumber";
    @BeforeEach
    void init() {
        bankAccountNumberResponseClientGateway = new ClientGateway<>(restClient, requestTransformer::transform, responseTransformer::transform,
                responseValidator::validate, ExecutorConfig.workStealingPool());
    }

    @Test
    void getAccountBalancesWithAllAnswers() {

        BankAccountNumberResponse bankAccountNumberResponse = MockHelper.getBankAccountNumberResponse();

        given(restClient.apply(any(Request.class))).willReturn(completedFuture(bankAccountNumberResponse));
        given(responseValidator.validate(bankAccountNumberResponse)).willReturn(bankAccountNumberResponse);
        given(requestTransformer.transform(anyString())).willReturn(request);
        given(responseTransformer.transform(any(), anyString())).willReturn(ACCOUNT_NUMBER);

        var bankAccountNumberResponseFuture = bankAccountNumberResponseClientGateway
                .performRequest("accountNumber");
        assertThat(bankAccountNumberResponseFuture.join()).isNotNull().isEqualTo(ACCOUNT_NUMBER);
    }

    @ParameterizedTest
    @ValueSource(strings = {"errors/InternalServerErrorItem.json", "errors/BadRequest.json", "errors/NotFoundErrorItem.json"})
    void checkForClientException(String filename) {

        CompletableFuture<BankAccountNumberResponse> badFuture = new CompletableFuture<>();
        ErrorItem errorItem = MockHelper.CreateErrorItem((filename));
        ClientException clientException = new ClientException(errorItem.getCode(), ErrorSource.ABA, errorItem);
        badFuture.completeExceptionally(clientException);
        given(restClient.apply(any(Request.class))).willReturn(badFuture);
        given(requestTransformer.transform(anyString())).willReturn(request);

        var expectedOutput = bankAccountNumberResponseClientGateway
                .performRequest(ACCOUNT_NUMBER);
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(errorItem.getCode()).isEqualTo(((ClientException) exception.getCause()).getErrorItem().getCode());

    }

    @Test
    void checkForEmptyResponse() {

        CompletableFuture<BankAccountNumberResponse> failingFuture = new CompletableFuture<>();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        failingFuture.completeExceptionally(bgosException);
        //GIVEN
        given(restClient.apply(any(Request.class))).willReturn(failingFuture);
        given(requestTransformer.transform(anyString())).willReturn(request);

        var expectedOutput = bankAccountNumberResponseClientGateway.performRequest(ACCOUNT_NUMBER);
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

    @Test
    void checkForConstraintValidation() {

        BankAccountNumberResponse bankAccountNumberResponse = MockHelper.getBankAccountNumberResponse();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);

        given(restClient.apply(any(Request.class))).willReturn(completedFuture(bankAccountNumberResponse));
        given(requestTransformer.transform(anyString())).willReturn(request);
        given(responseValidator.validate(bankAccountNumberResponse)).willThrow(bgosException);
        var expectedOutput = bankAccountNumberResponseClientGateway.performRequest(ACCOUNT_NUMBER);

        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

}

package com.ing.bankguarantees.remote.rest.permission;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.models.enums.ServiceActivitiesCode;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.rest.permission.model.request.PermissionRequest;
import com.ing.bankguarantees.remote.rest.permission.model.response.PermissionResponse;
import com.ing.bankguarantees.remote.rest.permission.transformer.PermissionRequestTransformer;
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
class PermissionClientGatewayTest
{
    @Mock
    private Request request;

    @Mock
    private JavaService<Request, PermissionResponse> restClient;

    @Mock
    private ResponseValidator<PermissionResponse> responseValidator;

    @Mock
    private PermissionRequestTransformer requestTransformer;

    private ClientGateway<PermissionRequest, PermissionResponse, PermissionResponse> permissionResponseClientGateway;

    @BeforeEach
    void init() {
        permissionResponseClientGateway = new ClientGateway<>(restClient, requestTransformer::transform,
                responseValidator::validate, ExecutorConfig.workStealingPool());
    }

    @Test
    void getPermissionResponseClientGatewayWithAllAnswers() {


        PermissionResponse permissionResponse = MockHelper.getPermissionResponse();
        PermissionRequest permissionRequest = MockHelper.getPermissionRequest(ServiceActivitiesCode.ORGANIZATION, "abc");
        given(restClient.apply(any(Request.class))).willReturn(completedFuture(permissionResponse));
        given(responseValidator.validate(permissionResponse)).willReturn(permissionResponse);
        given(requestTransformer.transform(permissionRequest)).willReturn(request);

        CompletableFuture<PermissionResponse> nameScreeningResponseCompletableFuture =
                permissionResponseClientGateway.performRequestWithValidate(permissionRequest);
        assertThat(nameScreeningResponseCompletableFuture.join()).isNotNull().isEqualTo(permissionResponse);
    }

    @ParameterizedTest
    @ValueSource(strings = {"errors/InternalServerErrorItem.json", "errors/BadRequest.json", "errors/NotFoundErrorItem.json"})
    void checkForClientException(String filename) {

        PermissionRequest permissionRequest = MockHelper.getPermissionRequest(ServiceActivitiesCode.ORGANIZATION, "abc");
        CompletableFuture<PermissionResponse> badFuture = new CompletableFuture<>();
        ErrorItem errorItem = MockHelper.CreateErrorItem((filename));
        ClientException clientException = new ClientException(errorItem.getCode(), ErrorSource.PMS, errorItem);
        badFuture.completeExceptionally(clientException);
        given(restClient.apply(any(Request.class))).willReturn(badFuture);
        given(requestTransformer.transform(any(PermissionRequest.class))).willReturn(request);

        CompletableFuture<PermissionResponse> permissionResponseCompletableFuture =
                permissionResponseClientGateway.performRequestWithValidate(permissionRequest);
        Exception exception = assertThrows(ExecutionException.class, permissionResponseCompletableFuture::get);
        assertThat(errorItem.getCode()).isEqualTo(((ClientException) exception.getCause()).getErrorItem().getCode());

    }

    @Test
    void checkForEmptyResponse() {

        PermissionRequest permissionRequest = MockHelper.getPermissionRequest(ServiceActivitiesCode.ORGANIZATION, "abc");
        CompletableFuture<PermissionResponse> failingFuture = new CompletableFuture<>();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        failingFuture.completeExceptionally(bgosException);
        //GIVEN
        given(restClient.apply(any(Request.class))).willReturn(failingFuture);
        given(requestTransformer.transform(any(PermissionRequest.class))).willReturn(request);

        CompletableFuture<PermissionResponse> permissionResponseCompletableFuture =
                permissionResponseClientGateway.performRequestWithValidate(permissionRequest);
        Exception exception = assertThrows(ExecutionException.class, permissionResponseCompletableFuture::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

    @Test
    void checkForConstraintValidation() {

        PermissionResponse permissionResponse = MockHelper.getPermissionResponse();
        PermissionRequest permissionRequest = MockHelper.getPermissionRequest(ServiceActivitiesCode.ORGANIZATION, "abc");
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        given(responseValidator.validate(permissionResponse)).willThrow(bgosException);

        given(restClient.apply(any(Request.class))).willReturn(completedFuture(permissionResponse));
        given(requestTransformer.transform(any(PermissionRequest.class))).willReturn(request);
        CompletableFuture<PermissionResponse> permissionResponseCompletableFuture =
                permissionResponseClientGateway.performRequestWithValidate(permissionRequest);
        Exception exception = assertThrows(ExecutionException.class, permissionResponseCompletableFuture::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

}

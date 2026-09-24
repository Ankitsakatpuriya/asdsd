package com.ing.bankguarantees.remote.rest.involveparty.invloveparties;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.Identifier;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.request.InvolvePartyRequest;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.OrganisationOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.transformer.InvPartyIndividualResTransformer;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.transformer.InvPartyInternalIdentifierResTransformer;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.transformer.InvPartyOrgResTransformer;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.transformer.InvPartyReqTransformer;
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
class InvolvePartiesClientGatewayTest {
    public static final String ORG_ID = "123456789";

    @Mock
    private Request request;
    @Mock
    private JavaService<Request, InvolvedPartyOnePamResponse> restClient;
    @Mock
    private ResponseValidator<InvolvedPartyOnePamResponse> responseValidator;
    @Mock(name = "requestTransformer")
    private InvPartyReqTransformer requestTransformer;
    @Mock(name = "responseTransformer")
    private InvPartyOrgResTransformer responseTransformer;
    private InvolvePartyRequest involvePartyRequest;

    @Mock
    private JavaService<Request, InvolvedPartyOnePamResponse> invPartyOnePamRestClient;
    @Mock
    private InvPartyReqTransformer invPartyReqTransformer;
    @Mock
    private InvPartyIndividualResTransformer invPartyIndividualResTransformer;
    @Mock
    private ResponseValidator<InvolvedPartyOnePamResponse> validator;

    @Mock
    private InvPartyInternalIdentifierResTransformer invPartyInternalIdentifierResTransformer;

    private ClientGateway<InvolvePartyRequest, OrganisationOnePamResponse, InvolvedPartyOnePamResponse> involvePartyClientGateway;
    public ClientGateway<InvolvePartyRequest, InvolvedPartyOnePamResponse.IndividualOnePamResponse, InvolvedPartyOnePamResponse> involvePartyIndividualClientGateway;
    public ClientGateway<InvolvePartyRequest, List<Identifier>, InvolvedPartyOnePamResponse> involvePartyInternalIdentifierClientGateway;

    @BeforeEach
    void init() {
        involvePartyClientGateway = new ClientGateway<>(restClient, requestTransformer::transform, (res, in) -> responseTransformer.transform(res), responseValidator::validate, ExecutorConfig.workStealingPool());
        involvePartyIndividualClientGateway = new ClientGateway<>(invPartyOnePamRestClient, invPartyReqTransformer::transform, (res, in) -> invPartyIndividualResTransformer.transform(res), validator::validate, ExecutorConfig.workStealingPool());
        involvePartyInternalIdentifierClientGateway = new ClientGateway<>(invPartyOnePamRestClient, invPartyReqTransformer::transform, (res1, in) -> invPartyInternalIdentifierResTransformer.transform(res1,in), validator::validate, ExecutorConfig.workStealingPool());
        involvePartyRequest = RequestAdapter.getInvolvePartyRequest(ORG_ID, Boolean.FALSE);
    }

    @Test
    void getInvolvePartyForOrgId() {

        InvolvedPartyOnePamResponse involvedPartyOrhResponse = MockHelper.getInvolvedPartyOnePamResponse();

        given(restClient.apply(any(Request.class))).willReturn(completedFuture(involvedPartyOrhResponse));
        given(responseValidator.validate(involvedPartyOrhResponse)).willReturn(involvedPartyOrhResponse);
        given(requestTransformer.transform(any(InvolvePartyRequest.class))).willReturn(request);
        given(responseTransformer.transform(involvedPartyOrhResponse)).willReturn(involvedPartyOrhResponse.getOrganisation());
        CompletableFuture<OrganisationOnePamResponse> actualOrgInvolveParty = involvePartyClientGateway.performRequest(involvePartyRequest);
        assertThat(actualOrgInvolveParty.join()).isNotNull().isEqualTo(involvedPartyOrhResponse.getOrganisation());
    }

    @ParameterizedTest
    @ValueSource(strings = {"errors/InternalServerErrorItem.json", "errors/BadRequest.json", "errors/NotFoundErrorItem.json"})
    void checkForClientException(String filename) {

        CompletableFuture<InvolvedPartyOnePamResponse> badFuture = new CompletableFuture<>();
        ErrorItem errorItem = MockHelper.CreateErrorItem((filename));
        ClientException clientException = new ClientException(errorItem.getCode(), ErrorSource.IPA, errorItem);
        badFuture.completeExceptionally(clientException);
        given(restClient.apply(any(Request.class))).willReturn(badFuture);
        given(requestTransformer.transform(any(InvolvePartyRequest.class))).willReturn(request);

        CompletableFuture<OrganisationOnePamResponse> expectedOutput = involvePartyClientGateway.performRequest(involvePartyRequest);
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(errorItem.getCode()).isEqualTo(((ClientException) exception.getCause()).getErrorItem().getCode());

    }

    @Test
    void checkForEmptyResponse() {

        CompletableFuture<InvolvedPartyOnePamResponse> failingFuture = new CompletableFuture<>();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        failingFuture.completeExceptionally(bgosException);
        given(restClient.apply(any(Request.class))).willReturn(failingFuture);
        given(requestTransformer.transform(any(InvolvePartyRequest.class))).willReturn(request);

        CompletableFuture<OrganisationOnePamResponse> expectedOutput = involvePartyClientGateway.performRequest(involvePartyRequest);
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

    @Test
    void checkForConstraintValidation() {

        InvolvedPartyOnePamResponse involvedPartyResponse = MockHelper.getInvolvedPartyOnePamResponse();
        involvedPartyResponse.setOrganisation(null);
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        given(restClient.apply(any(Request.class))).willReturn(completedFuture(involvedPartyResponse));
        given(requestTransformer.transform(any(InvolvePartyRequest.class))).willReturn(request);
        given(responseValidator.validate(involvedPartyResponse)).willThrow(bgosException);
        CompletableFuture<OrganisationOnePamResponse> expectedOutput = involvePartyClientGateway.performRequest(involvePartyRequest);
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

    @Test
    void getInvolvePartyForIndividual() {

        InvolvedPartyOnePamResponse involvedPartyOrhResponse = MockHelper.getInvolvedPartyOnePamResponse();

        given(invPartyOnePamRestClient.apply(any(Request.class))).willReturn(completedFuture(involvedPartyOrhResponse));
        given(validator.validate(involvedPartyOrhResponse)).willReturn(involvedPartyOrhResponse);
        given(invPartyReqTransformer.transform(any(InvolvePartyRequest.class))).willReturn(request);
        given(invPartyIndividualResTransformer.transform(involvedPartyOrhResponse)).willReturn(involvedPartyOrhResponse.getIndividual());
        CompletableFuture<InvolvedPartyOnePamResponse.IndividualOnePamResponse> actualOrgInvolveParty = involvePartyIndividualClientGateway.performRequest(involvePartyRequest);
        assertThat(actualOrgInvolveParty.join()).isNotNull().isEqualTo(involvedPartyOrhResponse.getIndividual());
    }

    @ParameterizedTest
    @ValueSource(strings = {"errors/InternalServerErrorItem.json", "errors/BadRequest.json", "errors/NotFoundErrorItem.json"})
    void checkForClientExceptionIndividual(String filename) {

        CompletableFuture<InvolvedPartyOnePamResponse> badFuture = new CompletableFuture<>();
        ErrorItem errorItem = MockHelper.CreateErrorItem((filename));
        ClientException clientException = new ClientException(errorItem.getCode(), ErrorSource.IPA, errorItem);
        badFuture.completeExceptionally(clientException);
        given(invPartyOnePamRestClient.apply(any(Request.class))).willReturn(badFuture);
        given(invPartyReqTransformer.transform(any(InvolvePartyRequest.class))).willReturn(request);

        CompletableFuture<InvolvedPartyOnePamResponse.IndividualOnePamResponse> expectedOutput = involvePartyIndividualClientGateway.performRequest(involvePartyRequest);
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(errorItem.getCode()).isEqualTo(((ClientException) exception.getCause()).getErrorItem().getCode());

    }

    @Test
    void checkForEmptyResponseIndividual() {

        CompletableFuture<InvolvedPartyOnePamResponse> failingFuture = new CompletableFuture<>();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        failingFuture.completeExceptionally(bgosException);
        given(invPartyOnePamRestClient.apply(any(Request.class))).willReturn(failingFuture);
        given(invPartyReqTransformer.transform(any(InvolvePartyRequest.class))).willReturn(request);

        CompletableFuture<InvolvedPartyOnePamResponse.IndividualOnePamResponse> expectedOutput = involvePartyIndividualClientGateway.performRequest(involvePartyRequest);
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

    @Test
    void checkForConstraintValidationIndividual() {

        InvolvedPartyOnePamResponse involvedPartyResponse = MockHelper.getInvolvedPartyOnePamResponse();
        involvedPartyResponse.setOrganisation(null);
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        given(invPartyOnePamRestClient.apply(any(Request.class))).willReturn(completedFuture(involvedPartyResponse));
        given(invPartyReqTransformer.transform(any(InvolvePartyRequest.class))).willReturn(request);
        given(validator.validate(involvedPartyResponse)).willThrow(bgosException);
        CompletableFuture<InvolvedPartyOnePamResponse.IndividualOnePamResponse> expectedOutput = involvePartyIndividualClientGateway.performRequest(involvePartyRequest);
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

    @Test
    void getInvolvePartyForIdentifierList() {

        InvolvedPartyOnePamResponse involvedPartyOrhResponse = MockHelper.getInvolvedPartyOnePamResponse();
        given(invPartyOnePamRestClient.apply(any(Request.class))).willReturn(completedFuture(involvedPartyOrhResponse));
        given(validator.validate(involvedPartyOrhResponse)).willReturn(involvedPartyOrhResponse);
        given(invPartyReqTransformer.transform(any(InvolvePartyRequest.class))).willReturn(request);
        CompletableFuture<List<Identifier>> actualOrgInvolveParty = involvePartyInternalIdentifierClientGateway.performRequest(involvePartyRequest);
        assertThat(actualOrgInvolveParty.join()).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"errors/InternalServerErrorItem.json", "errors/BadRequest.json", "errors/NotFoundErrorItem.json"})
    void checkForClientExceptionIdentifierList(String filename) {

        CompletableFuture<InvolvedPartyOnePamResponse> badFuture = new CompletableFuture<>();
        ErrorItem errorItem = MockHelper.CreateErrorItem((filename));
        ClientException clientException = new ClientException(errorItem.getCode(), ErrorSource.IPA, errorItem);
        badFuture.completeExceptionally(clientException);
        given(invPartyOnePamRestClient.apply(any(Request.class))).willReturn(badFuture);
        given(invPartyReqTransformer.transform(any(InvolvePartyRequest.class))).willReturn(request);

        CompletableFuture<List<Identifier>> expectedOutput = involvePartyInternalIdentifierClientGateway.performRequest(involvePartyRequest);
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(errorItem.getCode()).isEqualTo(((ClientException) exception.getCause()).getErrorItem().getCode());

    }

    @Test
    void checkForEmptyResponseIdentifierList() {

        CompletableFuture<InvolvedPartyOnePamResponse> failingFuture = new CompletableFuture<>();
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        failingFuture.completeExceptionally(bgosException);
        given(invPartyOnePamRestClient.apply(any(Request.class))).willReturn(failingFuture);
        given(invPartyReqTransformer.transform(any(InvolvePartyRequest.class))).willReturn(request);

        CompletableFuture<List<Identifier>> expectedOutput = involvePartyInternalIdentifierClientGateway.performRequest(involvePartyRequest);
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }

    @Test
    void checkForConstraintValidationIdentifierList() {

        InvolvedPartyOnePamResponse involvedPartyResponse = MockHelper.getInvolvedPartyOnePamResponse();
        involvedPartyResponse.setOrganisation(null);
        BgosException bgosException = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        given(invPartyOnePamRestClient.apply(any(Request.class))).willReturn(completedFuture(involvedPartyResponse));
        given(invPartyReqTransformer.transform(any(InvolvePartyRequest.class))).willReturn(request);
        given(validator.validate(involvedPartyResponse)).willThrow(bgosException);
        CompletableFuture<List<Identifier>> expectedOutput = involvePartyInternalIdentifierClientGateway.performRequest(involvePartyRequest);
        Exception exception = assertThrows(ExecutionException.class, expectedOutput::get);
        assertThat(((BgosException) exception.getCause()).getErrorCode()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
    }
}

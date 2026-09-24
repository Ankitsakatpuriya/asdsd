package com.ing.bankguarantees.service.involveparty;

import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.Identifier;
import com.ing.bankguarantees.models.domain.InvolvePartyData;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.request.InvolvePartyRequest;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.IdentifierOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.IndividualOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.OrganisationOnePamResponse;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class InvolvePartyDetailsServiceTest {

    private static final String LEGAL_ENTITY_ID = "c135b9ae-d6ee-4898-b236-f33a400d09fd";
    private static final String LEGAL_REP_ID = "07873b5c-489d-43cb-9cba-77f81905e682";
    private static final String ORGANIZATION_RESPONSE_FILE = "BGA/BGR/one_pam_organization_response.json";
    private static final String INDIVIDUAL_RESPONSE_FILE = "BGA/BGR/one_pam_individual_response.json";
    private static final String SEGMENT_CODE = "SVC_SEGM_BE";
    private static final String INVOLVE_PARTY_RESPONSE_FILE = "BGA/BGR/involve_party_data_response.json";


    @Mock
    private ClientGateway<InvolvePartyRequest, OrganisationOnePamResponse, InvolvedPartyOnePamResponse> involvePartyOrgClientGateway;

    @Mock
    private ClientGateway<InvolvePartyRequest, IndividualOnePamResponse, InvolvedPartyOnePamResponse> involvePartyIndividualClientGateway;

    @Mock
    private ClientGateway<InvolvePartyRequest, List<Identifier>, InvolvedPartyOnePamResponse> involvePartyInternalIdentifierClientGateway;

    @Mock
    private ExecutorService executorService;

    private InvolvePartyDetailsService involvePartyDetailsService;

    @BeforeEach
    void setUp() {
        executorService = ExecutorConfig.workStealingPool();
        involvePartyDetailsService = new InvolvePartyDetailsService(involvePartyOrgClientGateway, involvePartyIndividualClientGateway, involvePartyInternalIdentifierClientGateway);
    }

    @Test
    void getInternalIdentifierResponsePositive() {
        InvolvedPartyOnePamResponse involvedPartyIndividualResponse = MockHelper.createIndividualOnePamResponse(INDIVIDUAL_RESPONSE_FILE);
        List<IdentifierOnePamResponse> involvedPartyInternalIdentifiers = involvedPartyIndividualResponse.getIndividual().getInvolvedPartyInternalIdentifiers();
        List<Identifier> identifiers = MockHelper.convertOnePamIdentifier(involvedPartyInternalIdentifiers);
        given(involvePartyInternalIdentifierClientGateway.performRequest(any(InvolvePartyRequest.class))).willReturn(CompletableFuture.completedFuture(identifiers));
        List<Identifier> expectedIdentifiers = involvePartyDetailsService.getInternalIdentifierResponse(LEGAL_ENTITY_ID, true).join();
        assertThat(expectedIdentifiers).isNotNull();
        assertThat(expectedIdentifiers).isEqualTo(identifiers);
    }

    @Test
    void getInternalIdentifierResponseError() {

        given(involvePartyInternalIdentifierClientGateway.performRequest(any(InvolvePartyRequest.class))).willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<List<Identifier>> identifierFuture = involvePartyDetailsService.getInternalIdentifierResponse(LEGAL_ENTITY_ID, true);
        CompletionException exception = assertThrows(CompletionException.class, identifierFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }

    @Test
    void getInvolvedPartyOrganisationResponsePositive() {
        InvolvedPartyOnePamResponse involvedPartyOrganizationResponse = MockHelper.createOrganizationOnePamResponse(ORGANIZATION_RESPONSE_FILE);
        OrganisationOnePamResponse organisationOnePamResponse = involvedPartyOrganizationResponse.getOrganisation();
        given(involvePartyOrgClientGateway.performRequest(any(InvolvePartyRequest.class))).willReturn(CompletableFuture.completedFuture(organisationOnePamResponse));
        OrganisationOnePamResponse expectedResponse = involvePartyDetailsService.getInvolvedPartyOrganisationResponse(LEGAL_ENTITY_ID).join();
        assertThat(expectedResponse).isNotNull();
        assertThat(expectedResponse).isEqualTo(organisationOnePamResponse);
    }

    @Test
    void getInvolvedPartyDataPositive() {
        InvolvedPartyOnePamResponse involvedPartyOrganizationResponse = MockHelper.createOrganizationOnePamResponse(ORGANIZATION_RESPONSE_FILE);
        OrganisationOnePamResponse organisationOnePamResponse = involvedPartyOrganizationResponse.getOrganisation();
        InvolvePartyData involvePartyData = MockHelper.createInvolvePartyDataResponse(INVOLVE_PARTY_RESPONSE_FILE);
        given(involvePartyOrgClientGateway.performRequest(any(InvolvePartyRequest.class))).willReturn(CompletableFuture.completedFuture(organisationOnePamResponse));
        InvolvePartyData expectedResponse = involvePartyDetailsService.getInvolvePartyData(LEGAL_ENTITY_ID).join();
        assertThat(expectedResponse).isNotNull();
        assertThat(expectedResponse).isEqualTo(involvePartyData);
    }

    @ParameterizedTest
    @MethodSource("wholesaleBankingCustomerCode")
    void checkForWholeBankingCustomerCode(String involvePartyGroupCode) {
        InvolvedPartyOnePamResponse involvedPartyOrganizationResponse = MockHelper.createOrganizationOnePamResponse(ORGANIZATION_RESPONSE_FILE);
        OrganisationOnePamResponse organisationOnePamResponse = involvedPartyOrganizationResponse.getOrganisation();
        organisationOnePamResponse.getInvolvedPartyGroups().get(0).setInvolvedPartyGroupCode(involvePartyGroupCode);
        given(involvePartyOrgClientGateway.performRequest(any(InvolvePartyRequest.class))).willReturn(CompletableFuture.completedFuture(organisationOnePamResponse));
        InvolvePartyData expectedResponse = involvePartyDetailsService.getInvolvePartyData(LEGAL_ENTITY_ID).join();
        assertThat(expectedResponse).isNotNull();
        Assertions.assertTrue(expectedResponse.isWbCustomer());
    }

    @ParameterizedTest
    @MethodSource("normalCustomerCode")
    void checkForNormalCustomer(String involvePartyGroupCode) {
        InvolvedPartyOnePamResponse involvedPartyOrganizationResponse = MockHelper.createOrganizationOnePamResponse(ORGANIZATION_RESPONSE_FILE);
        OrganisationOnePamResponse organisationOnePamResponse = involvedPartyOrganizationResponse.getOrganisation();
        organisationOnePamResponse.getInvolvedPartyGroups().get(0).setInvolvedPartyGroupCode(involvePartyGroupCode);
        given(involvePartyOrgClientGateway.performRequest(any(InvolvePartyRequest.class))).willReturn(CompletableFuture.completedFuture(organisationOnePamResponse));
        InvolvePartyData expectedResponse = involvePartyDetailsService.getInvolvePartyData(LEGAL_ENTITY_ID).join();
        assertThat(expectedResponse).isNotNull();
        Assertions.assertFalse(expectedResponse.isWbCustomer());
    }

    private static Stream<String> normalCustomerCode() {
        return Stream.of("19", "18", "29", "1112", "2222", "4444", "1119", "1888", "2229");
    }

    private static Stream<String> wholesaleBankingCustomerCode() {
        return Stream.of("119", "118", "219", "138", "229", "299", "199", "188");
    }

    @Test
    void getInvolvedPartyOrganisationResponseError() {

        given(involvePartyOrgClientGateway.performRequest(any(InvolvePartyRequest.class))).willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<OrganisationOnePamResponse> organisationFuture = involvePartyDetailsService.getInvolvedPartyOrganisationResponse(LEGAL_ENTITY_ID);
        CompletionException exception = assertThrows(CompletionException.class, organisationFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }

    @Test
    void getInvolvePartyIndividualRespPositive() {
        InvolvedPartyOnePamResponse involvedPartyOrganizationResponse = MockHelper.createIndividualOnePamResponse(INDIVIDUAL_RESPONSE_FILE);
        IndividualOnePamResponse individual = involvedPartyOrganizationResponse.getIndividual();
        given(involvePartyIndividualClientGateway.performRequest(any(InvolvePartyRequest.class))).willReturn(CompletableFuture.completedFuture(individual));
        IndividualOnePamResponse expectedResponse = involvePartyDetailsService.getInvolvePartyIndividualResp(LEGAL_REP_ID).join();
        assertThat(expectedResponse).isNotNull();
        assertThat(expectedResponse).isEqualTo(individual);
    }

    @Test
    void getInvolvePartyIndividualRespError() {
        given(involvePartyIndividualClientGateway.performRequest(any(InvolvePartyRequest.class))).willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<IndividualOnePamResponse> involvePartyIndividualFuture = involvePartyDetailsService.getInvolvePartyIndividualResp(LEGAL_REP_ID);
        CompletionException exception = assertThrows(CompletionException.class, involvePartyIndividualFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }
}
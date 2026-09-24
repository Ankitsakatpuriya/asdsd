package com.ing.bankguarantees.service.instructingparty;

import com.ing.bankguarantees.models.response.InstructingPartyResponse;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.model.response.ExternalIdentifierListResponse;
import com.ing.bankguarantees.remote.rest.involveparty.grantees.model.response.GranteeGrantorResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.request.InvolvePartyRequest;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.OrganisationOnePamResponse;
import com.ing.bankguarantees.saac.BankGuaranteeSemAEventsHandler;
import com.ing.bankguarantees.service.involveparty.InvolvePartyDetailsService;
import com.ing.bankguarantees.service.involveparty.InvolvePartyGranteeService;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@Slf4j
@ExtendWith(MockitoExtension.class)
class InstructingPartyDetailServiceTest {

    @InjectMocks
    private InstructingPartyDetailService instructingPartyDetailService;

    @Mock
    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Mock
    private InvolvePartyGranteeService involvePartyGranteeService;

    @Mock
    private InvolvePartyDetailsService involvePartyDetailsService;

    @Mock
    private ClientGateway<String, Optional<String>, ExternalIdentifierListResponse> externalIdentifierGateway;

    @Mock
    private BankGuaranteeSemAEventsHandler semAEventsHandler;

    private static final String CBE_NO = "123456";


    @Test
    void getInstructingPartyEmailAddressDetailsTest() {
        given(externalIdentifierGateway.performRequest(any()))
                .willReturn(CompletableFuture.completedFuture(Optional.of(CBE_NO)));
        InvolvedPartyOnePamResponse involvedPartyOrgResponse = MockHelper.getInvolvedPartyOnePamResponse();
        given(involvePartyDetailsService.getInvolvedPartyOrganisationResponse(anyString()))
                .willReturn(CompletableFuture.completedFuture(involvedPartyOrgResponse.getOrganisation()));
        given(involvePartyDetailsService.getInvolvePartyIndividualResp(anyString()))
                .willReturn(CompletableFuture.completedFuture(MockHelper.getIndividualEmailDigitalAddressOnePamResponse()));
        given(involvePartyGranteeService.getLegalRepresentativeIds(anyString(), anyString()))
                .willReturn(CompletableFuture.completedFuture(List.of("2949e88c-943e-43fd-82d0-3342652a79be")));

        InstructingPartyResponse instructingPartyResponse = instructingPartyDetailService.getInstructingPartyDetails(MockHelper.getAccessToken(), "2456234").join();

        assertNotNull(instructingPartyResponse);
        assertThat(instructingPartyResponse.getIndividual()).isNotNull();
        assertThat(instructingPartyResponse.getOrganisation()).isNotNull();
        assertThat(instructingPartyResponse.getOrganisation().getCinNumber()).isEqualTo("123456");
        assertThat(instructingPartyResponse.getOrganisation().getLegalEntityId()).isEqualTo("2456234");

    }

    @Test
    void getInstructingPartyOrganizationEmpty() {
        given(externalIdentifierGateway.performRequest(any()))
                .willReturn(CompletableFuture.completedFuture(Optional.of(CBE_NO)));
        InvolvedPartyOnePamResponse involvedPartyOrgResponse = MockHelper.getInvolvedPartyOnePamResponse();
        involvedPartyOrgResponse.getOrganisation().setOrganisationNames(null);
        given(involvePartyDetailsService.getInvolvedPartyOrganisationResponse(anyString()))
                .willReturn(CompletableFuture.completedFuture(involvedPartyOrgResponse.getOrganisation()));
        given(involvePartyDetailsService.getInvolvePartyIndividualResp(anyString()))
                .willReturn(CompletableFuture.completedFuture(MockHelper.getIndividualEmailDigitalAddressOnePamResponse()));
        given(involvePartyGranteeService.getLegalRepresentativeIds(anyString(), anyString()))
                .willReturn(CompletableFuture.completedFuture(List.of("2949e88c-943e-43fd-82d0-3342652a79be")));

        CompletableFuture<InstructingPartyResponse> instructingPartyResponse = instructingPartyDetailService.getInstructingPartyDetails(MockHelper.getAccessToken(), "2456234");

        CompletionException exception = assertThrows(CompletionException.class, instructingPartyResponse::join);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("com.ing.bankguarantees.error.exception.BgosException: BGOS-00-010");
        assertThat(exception.getLocalizedMessage()).isEqualTo("com.ing.bankguarantees.error.exception.BgosException: BGOS-00-010");
    }

    @Test
    void getInstructingPartyPostalAddressEmpty() {
        given(externalIdentifierGateway.performRequest(any()))
                .willReturn(CompletableFuture.completedFuture(Optional.of(CBE_NO)));
        InvolvedPartyOnePamResponse involvedPartyOrgResponse = MockHelper.getInvolvedPartyOnePamResponse();
        involvedPartyOrgResponse.getOrganisation().setPostalAddresses(null);
        given(involvePartyDetailsService.getInvolvedPartyOrganisationResponse(anyString()))
                .willReturn(CompletableFuture.completedFuture(involvedPartyOrgResponse.getOrganisation()));
        given(involvePartyDetailsService.getInvolvePartyIndividualResp(anyString()))
                .willReturn(CompletableFuture.completedFuture(MockHelper.getIndividualEmailDigitalAddressOnePamResponse()));
        given(involvePartyGranteeService.getLegalRepresentativeIds(anyString(), anyString()))
                .willReturn(CompletableFuture.completedFuture(List.of("2949e88c-943e-43fd-82d0-3342652a79be")));

        CompletableFuture<InstructingPartyResponse> instructingPartyResponse = instructingPartyDetailService.getInstructingPartyDetails(MockHelper.getAccessToken(), "2456234");

        CompletionException exception = assertThrows(CompletionException.class, instructingPartyResponse::join);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("com.ing.bankguarantees.error.exception.BgosException: BGOS-00-009");
        assertThat(exception.getLocalizedMessage()).isEqualTo("com.ing.bankguarantees.error.exception.BgosException: BGOS-00-009");
    }

    @Test
    void getInstructingPartyValidateLegalRepo() {
        given(externalIdentifierGateway.performRequest(any()))
                .willReturn(CompletableFuture.completedFuture(Optional.of(CBE_NO)));
        InvolvedPartyOnePamResponse involvedPartyOrgResponse = MockHelper.getInvolvedPartyOnePamResponse();
        given(involvePartyDetailsService.getInvolvedPartyOrganisationResponse(anyString()))
                .willReturn(CompletableFuture.completedFuture(involvedPartyOrgResponse.getOrganisation()));
        given(involvePartyGranteeService.getLegalRepresentativeIds(anyString(), anyString()))
                .willReturn(CompletableFuture.completedFuture(List.of("2949e88c-943e-")));

        CompletableFuture<InstructingPartyResponse> instructingPartyResponse = instructingPartyDetailService.getInstructingPartyDetails(MockHelper.getAccessToken(), "2456234");

        CompletionException exception = assertThrows(CompletionException.class, instructingPartyResponse::join);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("com.ing.bankguarantees.error.exception.BgosException: BGOS-00-008");
        assertThat(exception.getLocalizedMessage()).isEqualTo("com.ing.bankguarantees.error.exception.BgosException: BGOS-00-008");
        verify(semAEventsHandler, atLeast(1)).publishSemEvent(any());
    }

    @Test
    void getInstructingPartyValidateLegalRepoForNull() {
        given(externalIdentifierGateway.performRequest(any()))
                .willReturn(CompletableFuture.completedFuture(null));
        InvolvedPartyOnePamResponse involvedPartyOrgResponse = MockHelper.getInvolvedPartyOnePamResponse();
        given(involvePartyDetailsService.getInvolvedPartyOrganisationResponse(anyString()))
                .willReturn(CompletableFuture.completedFuture(involvedPartyOrgResponse.getOrganisation()));
        given(involvePartyGranteeService.getLegalRepresentativeIds(anyString(), anyString()))
                .willReturn(CompletableFuture.completedFuture(List.of("2949e88c-943e-")));

        CompletableFuture<InstructingPartyResponse> instructingPartyResponse = instructingPartyDetailService.getInstructingPartyDetails(MockHelper.getAccessToken(), "2456234");

        CompletionException exception = assertThrows(CompletionException.class, instructingPartyResponse::join);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("com.ing.bankguarantees.error.exception.BgosException: BGOS-00-008");
        assertThat(exception.getLocalizedMessage()).isEqualTo("com.ing.bankguarantees.error.exception.BgosException: BGOS-00-008");
        verify(semAEventsHandler, atLeast(1)).publishSemEvent(any());
    }

}
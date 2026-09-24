package com.ing.bankguarantees.service.involveparty;

import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.domain.LegalRepresentativeData;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.involveparty.grantees.model.response.AssociatedPartyResponse;
import com.ing.bankguarantees.remote.rest.involveparty.grantees.model.response.GranteeGrantorResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.IndividualOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.OrganisationOnePamResponse;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class InvolvePartyGranteeServiceTest {

    private static final String LEGAL_ENTITY_ID = "c135b9ae-d6ee-4898-b236-f33a400d09fd";
    private static final String LEGAL_REP_ID = "07873b5c-489d-43cb-9cba-77f81905e682";
    private static final String ORGANIZATION_RESPONSE_FILE = "BGA/BGR/one_pam_organization_response.json";
    private static final String INDIVIDUAL_RESPONSE_FILE = "BGA/BGR/one_pam_individual_response.json";
    private static final String GRANTEE_RESPONSE_FILE = "BGA/BGR/one_pam_grantee_response.json";
    private static final String LEGAL_REP_NAME = "NESTORINETD BEN ALAMITD";
    private static final String LEGAL_REP_EMAIL = "dxgxkggk.tkdkiaxgg@nxxgz-xgdik.example.com";
    private static final String LEGAL_FORM_SOLE_PROPRIETOR = "BE_SOLE_PRPTRP";
    private static final String INDIVIDUAL_SOLE_PROPRIETOR = "SOLE_PRPTR";
    private static final String ORG_PARTY_TYPE = "ORG";
    private static final List<String> ALLOWED_RELATIONSHIP_TYPES = List.of("LGL_RPRS_LMTD_AHR", "PERM_REPN");
    private static final List<String> ALLOWED_GRANTEE_TYPES = List.of("LGL_REP", "PERM_REP");
    private static final List<String> POSITIVE_LR_IDS = List.of("43b87110-56e8-4388-abbd-a976e6bb8585",
            "07873b5c-489d-43cb-9cba-77f81905e682", "2ca57336-2f2a-4d1f-9dc1-3f2ae5ab3af4");

    @Mock
    private ClientGateway<String, GranteeGrantorResponse, GranteeGrantorResponse> invPartyGranteeGateway;

    @Mock
    private ExecutorService executorService;

    @Mock
    private InvolvePartyDetailsService involvePartyDetailsService;

    private InvolvePartyGranteeService involvePartyGranteeService;

    @BeforeEach
    void setUp() {
        executorService = ExecutorConfig.workStealingPool();
        involvePartyGranteeService = new InvolvePartyGranteeService(invPartyGranteeGateway, involvePartyDetailsService);
    }

    @Test
    void getAssociateLegalRepListPositive() {

        InvolvedPartyOnePamResponse involvedPartyOrganizationResponse = MockHelper.createOrganizationOnePamResponse(ORGANIZATION_RESPONSE_FILE);
        GranteeGrantorResponse granteeResponse = MockHelper.createGranteeResponse(GRANTEE_RESPONSE_FILE);
        LegalRepresentativeData legalRepresentativeData = MockHelper.getLegalRepresentativeData(LEGAL_REP_ID, LEGAL_REP_NAME, LEGAL_REP_EMAIL);
        InvolvedPartyOnePamResponse involvedPartyIndividualOnePamResponse = MockHelper.createIndividualOnePamResponse(INDIVIDUAL_RESPONSE_FILE);
        OrganisationOnePamResponse organisationOnePamResponse = involvedPartyOrganizationResponse.getOrganisation();
        IndividualOnePamResponse individualOnePamResponse = involvedPartyIndividualOnePamResponse.getIndividual();
        legalRepresentativeData.setInternalIdentifiers(CommonUtils.convertToBankGuaranteeIdentifiers(individualOnePamResponse.getInvolvedPartyInternalIdentifiers()));
        List<LegalRepresentativeData> expectedResponse = new ArrayList<>();
        expectedResponse.add(legalRepresentativeData);
        expectedResponse.add(legalRepresentativeData);
        expectedResponse.add(legalRepresentativeData);
        expectedResponse.add(legalRepresentativeData);

        given(invPartyGranteeGateway.performRequestWithValidate(LEGAL_ENTITY_ID)).willReturn(CompletableFuture.completedFuture(granteeResponse));
        given(involvePartyDetailsService.getInvolvePartyIndividualResp(any())).willReturn(CompletableFuture.completedFuture(individualOnePamResponse));
        given(involvePartyDetailsService.getInvolvePartyIndividualResp(any())).willReturn(CompletableFuture.completedFuture(individualOnePamResponse));
        given(involvePartyDetailsService.getInvolvePartyIndividualResp(any())).willReturn(CompletableFuture.completedFuture(individualOnePamResponse));
        given(involvePartyDetailsService.getInvolvePartyIndividualResp(any())).willReturn(CompletableFuture.completedFuture(individualOnePamResponse));
        List<LegalRepresentativeData> actualResponse = involvePartyGranteeService.getAssociateLegalRepList(LEGAL_ENTITY_ID,
                organisationOnePamResponse.getLegalForm(), LEGAL_REP_ID, "dxgxkggk.tkdkiaxgg@nxxgz-xgdik.example.com").join();

        assertThat(actualResponse).isNotEmpty();
        assertThat(actualResponse).isEqualTo(expectedResponse);

    }


    @Test
    void getAssociateLegalRepListSoleProprietor() {

        InvolvedPartyOnePamResponse involvedPartyOrganizationResponse = MockHelper.createOrganizationOnePamResponse(ORGANIZATION_RESPONSE_FILE);
        GranteeGrantorResponse granteeResponse = MockHelper.createGranteeResponse(GRANTEE_RESPONSE_FILE);
        setSoleProprietor(granteeResponse);
        LegalRepresentativeData legalRepresentativeData = MockHelper.getLegalRepresentativeData(LEGAL_REP_ID, LEGAL_REP_NAME, LEGAL_REP_EMAIL);
        InvolvedPartyOnePamResponse involvedPartyIndividualOnePamResponse = MockHelper.createIndividualOnePamResponse(INDIVIDUAL_RESPONSE_FILE);
        OrganisationOnePamResponse organisationOnePamResponse = involvedPartyOrganizationResponse.getOrganisation();
        organisationOnePamResponse.setLegalForm(LEGAL_FORM_SOLE_PROPRIETOR);

        IndividualOnePamResponse individualOnePamResponse = involvedPartyIndividualOnePamResponse.getIndividual();
        legalRepresentativeData.setInternalIdentifiers(CommonUtils.convertToBankGuaranteeIdentifiers(individualOnePamResponse.getInvolvedPartyInternalIdentifiers()));
        List<LegalRepresentativeData> expectedResponse = new ArrayList<>();
        expectedResponse.add(legalRepresentativeData);

        given(invPartyGranteeGateway.performRequestWithValidate(LEGAL_ENTITY_ID)).willReturn(CompletableFuture.completedFuture(granteeResponse));
        given(involvePartyDetailsService.getInvolvePartyIndividualResp(any())).willReturn(CompletableFuture.completedFuture(individualOnePamResponse));

        List<LegalRepresentativeData> actualResponse = involvePartyGranteeService.getAssociateLegalRepList(LEGAL_ENTITY_ID, organisationOnePamResponse.getLegalForm(),
                LEGAL_REP_ID, "dxgxkggk.tkdkiaxgg@nxxgz-xgdik.example.com").join();

        assertThat(actualResponse).isNotEmpty();
        assertThat(actualResponse).isEqualTo(expectedResponse);

    }

    @Test
    void getAssociateLegalRepListError() {

        InvolvedPartyOnePamResponse involvedPartyOrganizationResponse = MockHelper.createOrganizationOnePamResponse(ORGANIZATION_RESPONSE_FILE);
        OrganisationOnePamResponse organisation = involvedPartyOrganizationResponse.getOrganisation();
        given(invPartyGranteeGateway.performRequestWithValidate(LEGAL_ENTITY_ID)).willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<List<LegalRepresentativeData>> legalRepFuture = involvePartyGranteeService.getAssociateLegalRepList(LEGAL_ENTITY_ID, organisation.getLegalForm(),
                LEGAL_REP_ID, null);
        CompletionException exception = assertThrows(CompletionException.class, legalRepFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }

    @Test
    void getLegalRepIdsPositive() {

        InvolvedPartyOnePamResponse involvedPartyOrganizationResponse = MockHelper.createOrganizationOnePamResponse(ORGANIZATION_RESPONSE_FILE);
        OrganisationOnePamResponse organisation = involvedPartyOrganizationResponse.getOrganisation();
        GranteeGrantorResponse granteeResponse = MockHelper.createGranteeResponse(GRANTEE_RESPONSE_FILE);
        given(invPartyGranteeGateway.performRequestWithValidate(LEGAL_ENTITY_ID)).willReturn(CompletableFuture.completedFuture(granteeResponse));
        List<String> lrIds = involvePartyGranteeService.getLegalRepresentativeIds(LEGAL_ENTITY_ID, organisation.getLegalForm()).join();
        assertThat(lrIds).isNotEmpty();
        assertThat(lrIds).containsAll(POSITIVE_LR_IDS);

    }

    @Test
    void getAssociateLegalRepUUIDNOTFound() {

        InvolvedPartyOnePamResponse involvedPartyOrganizationResponse = MockHelper.createOrganizationOnePamResponse(ORGANIZATION_RESPONSE_FILE);
        GranteeGrantorResponse granteeResponse = MockHelper.createGranteeResponse(GRANTEE_RESPONSE_FILE);
        InvolvedPartyOnePamResponse involvedPartyIndividualOnePamResponse = MockHelper.createIndividualOnePamResponse(INDIVIDUAL_RESPONSE_FILE);
        OrganisationOnePamResponse organisationOnePamResponse = involvedPartyOrganizationResponse.getOrganisation();
        IndividualOnePamResponse individualOnePamResponse = involvedPartyIndividualOnePamResponse.getIndividual();
        individualOnePamResponse.setInvolvedPartyInternalIdentifiers(new ArrayList<>());

        given(invPartyGranteeGateway.performRequestWithValidate(LEGAL_ENTITY_ID)).willReturn(CompletableFuture.completedFuture(granteeResponse));
        given(involvePartyDetailsService.getInvolvePartyIndividualResp(any())).willReturn(CompletableFuture.completedFuture(individualOnePamResponse));

        CompletableFuture<List<LegalRepresentativeData>> legalRepFuture = involvePartyGranteeService.getAssociateLegalRepList(LEGAL_ENTITY_ID, organisationOnePamResponse.getLegalForm(),
                LEGAL_REP_ID, "dxgxkggk.tkdkiaxgg@nxxgz-xgdik.example.com");
        CompletionException exception = assertThrows(CompletionException.class, legalRepFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }


    private void setSoleProprietor(GranteeGrantorResponse granteeResponse) {
        List<AssociatedPartyResponse> associatedPartyRelationships = granteeResponse.getInvolvedPartyInvolvedPartyRelationships().getAssociatedPartyRelationships();
        for (AssociatedPartyResponse associatedPartyResponse : associatedPartyRelationships) {
            String id = associatedPartyResponse.getGrantee().getId();
            String involvedPartyType = associatedPartyResponse.getGrantee().getType();
            if (involvedPartyType.equals("LGL_REP") && id.equals(LEGAL_REP_ID)) {
                associatedPartyResponse.getGrantee().setType(INDIVIDUAL_SOLE_PROPRIETOR);
            }
        }
    }

    @Test
    void getAssociateLegalRepListNegative() {

        InvolvedPartyOnePamResponse involvedPartyOrganizationResponse = MockHelper.createOrganizationOnePamResponse(ORGANIZATION_RESPONSE_FILE);
        GranteeGrantorResponse granteeResponse = MockHelper.createGranteeResponse(GRANTEE_RESPONSE_FILE);
        setInvolvePartyTypeORG(granteeResponse);
        OrganisationOnePamResponse organisationOnePamResponse = involvedPartyOrganizationResponse.getOrganisation();
        given(invPartyGranteeGateway.performRequestWithValidate(LEGAL_ENTITY_ID)).willReturn(CompletableFuture.completedFuture(granteeResponse));
        List<LegalRepresentativeData> actualResponse = involvePartyGranteeService.getAssociateLegalRepList(LEGAL_ENTITY_ID,
                organisationOnePamResponse.getLegalForm(), LEGAL_REP_ID, "dxgxkggk.tkdkiaxgg@nxxgz-xgdik.example.com").join();
        assertThat(actualResponse).isEmpty();

    }

    private void setInvolvePartyTypeORG(GranteeGrantorResponse granteeResponse) {
        List<AssociatedPartyResponse> associatedPartyRelationships = granteeResponse.getInvolvedPartyInvolvedPartyRelationships().getAssociatedPartyRelationships();
        for (AssociatedPartyResponse associatedPartyResponse : associatedPartyRelationships) {
            String id = associatedPartyResponse.getGrantee().getId();
            String involvedPartyType = associatedPartyResponse.getGrantee().getType();
            if (ALLOWED_GRANTEE_TYPES.contains(involvedPartyType) && ALLOWED_RELATIONSHIP_TYPES.contains(associatedPartyResponse.getInvolvedPartyInvolvedPartyRelationshipType())) {
                associatedPartyResponse.getGrantee().setInvolvedPartyType(ORG_PARTY_TYPE);
            }
        }
    }


}
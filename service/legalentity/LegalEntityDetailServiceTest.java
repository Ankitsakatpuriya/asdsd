package com.ing.bankguarantees.service.legalentity;

import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.response.LegalEntityResponse;
import com.ing.bankguarantees.models.response.LegalEntityResponse.LegalEntityPostalAddressResponse;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.request.CSIHubInvolvedPartiesRequest;
import com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.response.InvolvedPartiesCsiHubResponse;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static com.ing.bankguarantees.util.MockHelper.getInvolvedPartiesCsiHubResponseByKBOBEmptyOrganisation;
import static com.ing.bankguarantees.util.MockHelper.getInvolvedPartiesCsiHubResponseByKBOEmptyPostalAddress;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class LegalEntityDetailServiceTest {

    @Mock
    private ClientGateway<CSIHubInvolvedPartiesRequest, InvolvedPartiesCsiHubResponse, InvolvedPartiesCsiHubResponse> csiInvolvePartyGateway;

    @InjectMocks
    private LegalEntityDetailService legalEntityService;

    private InvolvedPartiesCsiHubResponse involvedPartiesCsiHubResponse;
    private static final String CBE_NO = "123";
    private static final String ERROR = "error";
    private static final String POSTAL_ADDRESS = "getPostalAddressOfKbo";

    @BeforeEach
    void setUp() {
        involvedPartiesCsiHubResponse = MockHelper.getInvolvedPartiesCsiHubResponse();
    }

    @Test
    void getLegalEntityByKBO() {
        given(csiInvolvePartyGateway.performRequestWithValidate(any()))
                .willReturn(CompletableFuture.completedFuture(involvedPartiesCsiHubResponse));

        LegalEntityResponse expected = legalEntityService.getLegalEntityByKBO(CBE_NO).join();

        assertNotNull(expected);
        assertThat(expected.getCinNumber()).isEqualTo("123");
        assertThat(expected.getOrganisationName()).isNotNull();
        assertThat(expected.getOrganisationName().getFullName()).isEqualTo("CSI SPRL");
        assertThat(expected.getEmailAddress().getEmailIdInformation()).isEqualTo("Email Address Information");
        assertThat(expected.getPostalAddress().getCityName()).isEqualTo("brussels");
    }

    @Test
    void getLegalEntityByKBOEmptyPostalAddress() {
        involvedPartiesCsiHubResponse = getInvolvedPartiesCsiHubResponseByKBOEmptyPostalAddress();
        given(csiInvolvePartyGateway.performRequestWithValidate(any()))
                .willReturn(CompletableFuture.completedFuture(involvedPartiesCsiHubResponse));
        CompletableFuture<LegalEntityResponse> legalEntityByKBO = legalEntityService.getLegalEntityByKBO(CBE_NO);
        CompletionException completionException = assertThrows(CompletionException.class, legalEntityByKBO::join);
        assertThat(completionException.getLocalizedMessage()).contains(ErrorCode.INC_ADDRESS_NOT_FOUND.getCode());
    }

    @Test
    void getLegalEntityByKBOBEmptyOrganisation() {

        involvedPartiesCsiHubResponse = getInvolvedPartiesCsiHubResponseByKBOBEmptyOrganisation();
        given(csiInvolvePartyGateway.performRequestWithValidate(any()))
                .willReturn(CompletableFuture.completedFuture(involvedPartiesCsiHubResponse));
        CompletableFuture<LegalEntityResponse> legalEntityByKBO = legalEntityService.getLegalEntityByKBO(CBE_NO);
        CompletionException completionException = assertThrows(CompletionException.class, legalEntityByKBO::join);

        assertThat(completionException.getLocalizedMessage()).contains(ErrorCode.INC_ORG_NAME_NOT_FOUND.getCode());
    }

    @Test
    void getLegalEntityByKBOeMPTYCsiInvolveParty() {
        given(csiInvolvePartyGateway.performRequestWithValidate(any()))
                .willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<LegalEntityResponse> legalEntityByKBO = legalEntityService.getLegalEntityByKBO(CBE_NO);
        CompletionException completionException = assertThrows(CompletionException.class, legalEntityByKBO::join);

        assertThat(completionException.getLocalizedMessage()).contains(ErrorCode.TECHNICAL_ERROR.getCode());
    }

    @Test
    void getLegalEntityResp() {
        CompletableFuture<InvolvedPartiesCsiHubResponse> badFuture = new CompletableFuture<>();
        ClientException clientException = new ClientException(ERROR, ErrorSource.BGOS, ErrorItem.builder().build());
        badFuture.completeExceptionally(clientException);
        given(csiInvolvePartyGateway.performRequestWithValidate(any()))
                .willReturn(badFuture);
        CompletableFuture<InvolvedPartiesCsiHubResponse> actual = legalEntityService.getLegalEntityResp(CBE_NO);
        ExecutionException exception = assertThrows(ExecutionException.class, actual::get);

        assertNotNull(exception);
        assertThat(exception.getLocalizedMessage()).isEqualTo("com.ing.bankguarantees.error.exception.ClientException: error");
    }

    @Test
    void getPostalAddressOfKbo() {
        Method privateMethod = null;
        InvolvedPartiesCsiHubResponse invPartyCsiRes = involvedPartiesCsiHubResponse;
        try {
            privateMethod = LegalEntityDetailService.class.getDeclaredMethod(POSTAL_ADDRESS, InvolvedPartiesCsiHubResponse.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        privateMethod.setAccessible(true);
        LegalEntityPostalAddressResponse expected = null;

        try {
            expected = (LegalEntityPostalAddressResponse) privateMethod.invoke(legalEntityService, invPartyCsiRes);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        assertNotNull(expected);
    }
}


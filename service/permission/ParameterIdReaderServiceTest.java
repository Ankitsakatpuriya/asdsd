package com.ing.bankguarantees.service.permission;

import com.ing.bankguarantees.database.BankGuaranteeRequestDao;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.enums.ServiceActivitiesCode;
import com.ing.bankguarantees.models.request.BankGuaranteeFinalizationPayload;
import com.ing.bankguarantees.models.request.BankGuaranteeRequestPayload;
import com.ing.bankguarantees.models.request.InstructingPartyPayload;
import com.ing.bankguarantees.models.request.SignDocumentPayload;
import com.ing.bankguarantees.util.MockHelper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ParameterIdReaderServiceTest {

    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";

    private BankGuaranteeRequestDao bankGuaranteeRequestDao;
    private ParameterIdReaderService parameterIdReaderService;

    @BeforeEach
    void setup() {
        bankGuaranteeRequestDao = mock(BankGuaranteeRequestDao.class);
        parameterIdReaderService = new ParameterIdReaderService(bankGuaranteeRequestDao);
    }

    private JoinPoint mockJoinPoint(String paramName, Object arg) throws NoSuchMethodException {
        JoinPoint joinPoint = mock(JoinPoint.class);
        Method method = this.getClass().getDeclaredMethod("dummyMethod", String.class);
        MethodSignature methodSignature = mock(MethodSignature.class);

        when(methodSignature.getParameterNames()).thenReturn(new String[]{paramName});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{arg});

        return joinPoint;
    }

    public void dummyMethod(String dummy) {}

    @Test
    void shouldReturnIdForOrganization() throws Exception {
        String expectedId = "ORG123";
        JoinPoint jp = mockJoinPoint(ServiceActivitiesCode.ORGANIZATION.getParameterName(), expectedId);

        String result = parameterIdReaderService.getId(ServiceActivitiesCode.ORGANIZATION, jp);
        assertThat(result).isEqualTo(expectedId);
    }

    @Test
    void shouldReturnIdForSubmit() throws Exception {
            String expectedId = "LEGAL123";
            BankGuaranteeRequestPayload payload = new BankGuaranteeRequestPayload();
            InstructingPartyPayload instructingParty = new InstructingPartyPayload();
            InstructingPartyPayload.OrganisationPayload organisation = new InstructingPartyPayload.OrganisationPayload();
            organisation.setLegalEntityId(expectedId);
            instructingParty.setOrganisation(organisation);
            payload.setInstructingParty(instructingParty);
            JoinPoint jp = mockJoinPoint(ServiceActivitiesCode.SUBMIT.getParameterName(), payload);
            String result = parameterIdReaderService.getId(ServiceActivitiesCode.SUBMIT, jp);
            assertThat(result).isEqualTo(expectedId);
    }

    @Test
    void shouldReturnIdForGetDocument() throws Exception {
        BankGuaranteeRequest entity = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);

        when(bankGuaranteeRequestDao.getBankGuaranteeEntity(REQUESTER_ID)).thenReturn(CompletableFuture.completedFuture(entity));
        JoinPoint jp = mockJoinPoint("requestId", REQUESTER_ID);

        String result = parameterIdReaderService.getId(ServiceActivitiesCode.GET_DOCUMENT, jp);
        assertThat(result).isEqualTo(UUID_ORG);
    }

    @Test
    void shouldReturnIdForSigning() throws Exception {
        SignDocumentPayload payload = MockHelper.getSignDocumentPayload(REQUESTER_ID);
        BankGuaranteeRequest entity = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        entity.setOrganisationId(UUID_ORG); //
        when(bankGuaranteeRequestDao.getBankGuaranteeEntity(payload.getRequestId()))
                .thenReturn(CompletableFuture.completedFuture(entity));
        JoinPoint jp = mockJoinPoint(ServiceActivitiesCode.SIGNING.getParameterName(), payload);
        String result = parameterIdReaderService.getId(ServiceActivitiesCode.SIGNING, jp);
        assertThat(result).isEqualTo(UUID_ORG);
    }

    @Test
    void shouldReturnIdForFinalization() throws Exception {
        BankGuaranteeFinalizationPayload payload =MockHelper.getBankGuaranteeFinalizationPayload(REQUESTER_ID);
        BankGuaranteeRequest entity = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        when(bankGuaranteeRequestDao.getBankGuaranteeEntity(REQUESTER_ID))
                .thenReturn(CompletableFuture.completedFuture(entity));
        JoinPoint jp = mockJoinPoint(ServiceActivitiesCode.FINALIZATION.getParameterName(), payload);
        String result = parameterIdReaderService.getId(ServiceActivitiesCode.FINALIZATION, jp);
        assertThat(result).isEqualTo(UUID_ORG);
    }

    @Test
    void shouldThrowExceptionWhenParameterNotFound() throws Exception {
        JoinPoint jp = mockJoinPoint("wrongParam", "value");

        assertThrows(Exception.class, () -> {
            parameterIdReaderService.getId(ServiceActivitiesCode.ORGANIZATION, jp);
        });
    }
}

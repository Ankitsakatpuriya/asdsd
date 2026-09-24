package com.ing.bankguarantees.service.permission;

import com.ing.bankguarantees.models.enums.SemEvent;
import com.ing.bankguarantees.models.enums.ServiceActivitiesCode;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.permission.model.request.PermissionRequest;
import com.ing.bankguarantees.remote.rest.permission.model.response.PermissionResponse;
import com.ing.bankguarantees.saac.BankGuaranteeSemAEventsHandler;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {


    private static final String ARG_ID = "12345";
    private static final String PERMISSION_RESP_ID = "2949e88c-943e-43fd-82d0-3342652a79be";
    private static final String PERMISSION_RESP_ID_NEG = "2949e88c-943e-43f";

    @Mock
    private ClientGateway<PermissionRequest, PermissionResponse, PermissionResponse> permissionGateway;

    @Mock
    private ParameterIdReaderService parameterIdReaderService;

    @Mock
    private BankGuaranteeSemAEventsHandler semAEventsHandler;

    @InjectMocks
    private PermissionService permissionService;


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(permissionService, "serviceActivityName", "involved-parties:bank-guarantees:create");
    }

    @Test
    void checkPermission() {
        PermissionResponse permissionResponse = MockHelper.getPermissionResponse();
        permissionResponse.setIdentifier(PERMISSION_RESP_ID);
        given(permissionGateway.performRequestWithValidate(any())).willReturn(CompletableFuture.completedFuture(permissionResponse));
        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{ARG_ID, MockHelper.getAccessToken()});
        when(parameterIdReaderService.getId(any(), any())).thenReturn("2949e88c-943e-43fd-82d0-3342652a79be");
        permissionService.checkLegalEntityPermission(ServiceActivitiesCode.ORGANIZATION, joinPoint);
        verify(permissionGateway, atLeast(1)).performRequestWithValidate(any());
    }

    @Test
    void checkPermissionNeg() {
        PermissionResponse permissionResponse = MockHelper.getPermissionResponse();
        permissionResponse.setIdentifier(PERMISSION_RESP_ID_NEG);
        given(permissionGateway.performRequestWithValidate(any())).willReturn(CompletableFuture.completedFuture(permissionResponse));
        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{ARG_ID, MockHelper.getAccessToken()});
        when(parameterIdReaderService.getId(any(), any())).thenReturn("2949e88c-943e-43fd-82d0-3342679be");
        assertThrows(BgosException.class, () -> permissionService.checkLegalEntityPermission(ServiceActivitiesCode.ORGANIZATION, joinPoint));
        verify(semAEventsHandler, atLeast(1)).publishSemEvent(any());

    }


    @Test
    void checkLegalEntityPermission() {
        PermissionResponse permissionResponse = MockHelper.getPermissionResponse();
        given(permissionGateway.performRequestWithValidate(any())).willReturn(CompletableFuture.completedFuture(permissionResponse));
        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{ARG_ID, MockHelper.getAccessToken()});
        when(parameterIdReaderService.getId(any(), any())).thenReturn("12345");
        permissionService.checkLegalEntityPermission(ServiceActivitiesCode.ORGANIZATION, joinPoint);
        verify(permissionGateway, atLeast(1)).performRequestWithValidate(any());
    }

    @Test
    void checkLegalEntityPermissionParameterIndexNegative() {
        PermissionResponse permissionResponse = MockHelper.getPermissionResponse();
        permissionResponse.getServiceActivities().get(0).setType("type");
        given(permissionGateway.performRequestWithValidate(any())).willReturn(CompletableFuture.completedFuture(permissionResponse));
        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{ARG_ID, MockHelper.getAccessToken()});
        when(parameterIdReaderService.getId(any(), any())).thenReturn("12345");
        doNothing().when(semAEventsHandler).publishSemEvent(any(SemEvent.class));
        assertThrows(BgosException.class, () -> permissionService.checkLegalEntityPermission(ServiceActivitiesCode.FINANCIAL, joinPoint));
        verify(semAEventsHandler, atLeast(1)).publishSemEvent(any());

    }

}
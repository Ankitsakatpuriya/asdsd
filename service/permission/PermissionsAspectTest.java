package com.ing.bankguarantees.service.permission;

import com.ing.bankguarantees.models.enums.ServiceActivitiesCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class PermissionsAspectTest {

    @InjectMocks
    PermissionsAspect permissionsAspect;

    @Mock
    PermissionService permissionService;

    @Mock
    CheckPermissions checkPermissions;

    @Test
    void checkPermissionsOrgId() {
        JoinPoint joinPoint = mock(JoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = mock(Method.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(method.getAnnotation(CheckPermissions.class)).thenReturn(checkPermissions);
        when(checkPermissions.serviceActivitiesCode()).thenReturn(ServiceActivitiesCode.ORGANIZATION);

        doNothing().when(permissionService).checkLegalEntityPermission(any(), any());
        permissionsAspect.checkPermissions(joinPoint);

        verify(permissionService, atLeast(1)).checkLegalEntityPermission(any(), any());
    }

}

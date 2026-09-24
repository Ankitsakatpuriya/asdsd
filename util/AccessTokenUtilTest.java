package com.ing.bankguarantees.util;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.apisdk.toolkit.trust.accesstoken.AccessTokenClaimsSet;
import com.ing.apisdk.toolkit.trust.accesstoken.Executor;
import com.ing.apisdk.toolkit.trust.accesstoken.Requester;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.utils.AccessTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class AccessTokenUtilTest {

    @Test
    void testGetRequesterPersonId_validRequesterAccessToken() {

        AccessToken mockAccessToken = Mockito.mock(AccessToken.class);
        AccessTokenClaimsSet mockClaimsSet = Mockito.mock(AccessTokenClaimsSet.class);
        when(mockAccessToken.getClaimsSet()).thenReturn(mockClaimsSet);
        Requester requester = Mockito.mock(Requester.class);
        when(mockClaimsSet.getRequester()).thenReturn(requester);
        when(requester.getPerson()).thenReturn(Optional.of("personId"));

        // Act
        String personId = AccessTokenUtil.getRequesterPersonId(mockAccessToken);

        // Assert
        assertEquals("personId", personId);
    }

    @Test
    void testGetRequesterPersonId_validExecutorAccessToken_customer() {

        AccessToken mockAccessToken = Mockito.mock(AccessToken.class);
        AccessTokenClaimsSet mockClaimsSet = Mockito.mock(AccessTokenClaimsSet.class);
        when(mockAccessToken.getClaimsSet()).thenReturn(mockClaimsSet);
        when(mockClaimsSet.getRequester()).thenReturn(null);

        Executor executor = Mockito.mock(Executor.class);
        when(mockClaimsSet.getExecutor()).thenReturn(executor);
        when(executor.getType()).thenReturn("customer");

        when(executor.getPerson()).thenReturn(Optional.of("personId"));

        // Act
        String personId = AccessTokenUtil.getRequesterPersonId(mockAccessToken);

        // Assert
        assertEquals("personId", personId);
    }

    @Test
    void testGetRequesterPersonId_validExecutorAccessToken_employee() {

        AccessToken mockAccessToken = Mockito.mock(AccessToken.class);
        AccessTokenClaimsSet mockClaimsSet = Mockito.mock(AccessTokenClaimsSet.class);
        when(mockAccessToken.getClaimsSet()).thenReturn(mockClaimsSet);
        when(mockClaimsSet.getRequester()).thenReturn(null);

        Executor executor = Mockito.mock(Executor.class);
        when(mockClaimsSet.getExecutor()).thenReturn(executor);
        when(executor.getType()).thenReturn("employee");

        when(executor.getPerson()).thenReturn(Optional.of("personId"));

        // Act
        String personId = AccessTokenUtil.getRequesterPersonId(mockAccessToken);

        // Assert
        assertEquals("personId", personId);
    }

    @Test
    void testGetRequesterPersonId_invalidAccessToken() {
        // Arrange
        AccessToken mockAccessToken = Mockito.mock(AccessToken.class);
        AccessTokenClaimsSet mockClaimsSet = Mockito.mock(AccessTokenClaimsSet.class);
        when(mockAccessToken.getClaimsSet()).thenReturn(mockClaimsSet);
        when(mockClaimsSet.getRequester()).thenReturn(null);

        Executor executor = Mockito.mock(Executor.class);
        when(mockClaimsSet.getExecutor()).thenReturn(executor);
        when(executor.getType()).thenReturn("employee1");
        // Act & Assert
        assertThrows(BgosException.class, () -> AccessTokenUtil.getRequesterPersonId(mockAccessToken));
    }

}

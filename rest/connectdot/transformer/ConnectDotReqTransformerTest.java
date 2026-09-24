package com.ing.bankguarantees.remote.rest.connectdot.transformer;

import com.ing.bankguarantees.remote.rest.connectdot.mapper.ConnectDotMapper;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotInput;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotRequest;
import com.twitter.finagle.http.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectDotReqTransformerTest {

    @Mock
    private ConnectDotMapper connectDotMapper;

    @Mock
    private ConnectDotRequest<?> mockConnectDotRequest;

    @InjectMocks
    private ConnectDotReqTransformer transformer;

    @BeforeEach
    void setup() {
        String url = "https://mock-url/connect-dot";
        transformer = new ConnectDotReqTransformer(url, connectDotMapper);
    }


    @Test
    void transformTest() {
        // Given
        ConnectDotInput input = new ConnectDotInput();
        ConnectDotRequest.Identifiers identifiers = ConnectDotRequest.Identifiers.builder()
                .requestId(UUID.randomUUID())
                .build();

        ConnectDotRequest<?> mockRequest = mock(ConnectDotRequest.class);
        when(mockRequest.getIdentifiers()).thenReturn(identifiers);
        when(connectDotMapper.prepareConnectDotRequest(input)).thenReturn((ConnectDotRequest) mockRequest);

        // When
        Request request = transformer.transform(input);

        // Then
        assertNotNull(request);
        verify(connectDotMapper).prepareConnectDotRequest(input);
    }

}

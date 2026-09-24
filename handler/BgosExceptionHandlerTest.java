package com.ing.bankguarantees.handler;


import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.HttpErrorResolver;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.error.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BgosExceptionHandlerTest {

    @Mock
    private HttpErrorResolver errorResolver;

    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private BgosExceptionHandler underTest;

    @BeforeEach
    void setup() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        request = new HttpServletRequestWrapper(mockHttpServletRequest);
    }

    @Test
    void handleBgosException() {
        given(errorResolver.resolve("BGOS-00-002", ErrorSource.BGOS))
                .willReturn(ErrorItem.builder().code("BGOS-00-002")
                        .severity("critical")
                        .message("Technical error")
                        .source("Internal Server Error")
                        .externalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .internalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());

        ResponseEntity<ErrorResponse> responseEntity =
                underTest.handleBgosException(new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT), request, response);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getError()).isNotNull();
        assertThat(responseEntity.getBody().getError().getCode()).isEqualTo("BGOS-00-002");
        assertThat(responseEntity.getBody().getError().getSeverity()).isEqualTo("critical");
        assertThat(responseEntity.getBody().getError().getMessage()).isEqualTo("Technical error");
    }

    @Test
    void handleClientException() {

        ErrorItem errorItem = ErrorItem.builder().code("BGOS-00-003")
                .severity("critical")
                .message("Bad request")
                .source("INV API")
                .externalHttpStatus(HttpStatus.BAD_REQUEST)
                .internalHttpStatus(HttpStatus.BAD_REQUEST)
                .build();

        ResponseEntity<ErrorResponse> responseEntity = underTest.handleClientException(new ClientException("BGOS-00-003",
                ErrorSource.BGOS,
                errorItem), request);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getError()).isNotNull();
        assertThat(responseEntity.getBody().getError().getCode()).isEqualTo("BGOS-00-003");
        assertThat(responseEntity.getBody().getError().getSeverity()).isEqualTo("critical");
        assertThat(responseEntity.getBody().getError().getMessage()).isEqualTo("Bad request");

    }

}
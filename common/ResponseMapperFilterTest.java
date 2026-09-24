package com.ing.bankguarantees.remote.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Await;
import com.twitter.util.Duration;
import com.twitter.util.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ResponseMapperFilterTest {

    private static final String TEST_STRING = "test";

    private ObjectMapper mapper;

    @Mock
    private Request request;
    @Mock
    private Response response;
    @Mock
    private Service<Request, Response> service;

    @BeforeEach
    void initMocks() {
        Mockito.when(service.apply(request)).thenReturn(Future.value(response));
        mapper = new ObjectMapper();
    }

    @Test
    void mapResponseSuccessfully() throws Exception {

        ResponseMapperFilter<TestResponse> underTest = new ResponseMapperFilter<>(TestResponse.class, mapper);
        TestResponse expectedResult = new TestResponse(TEST_STRING);
        Mockito.when(response.getContentString()).thenReturn(mapper.writeValueAsString(expectedResult));
        Future<TestResponse> result = underTest.apply(request, service);
        TestResponse actualResponse = Await.result(result, Duration.fromSeconds(10));
        assertThat(actualResponse.value()).isEqualTo(expectedResult.value());
    }

    @Test
    void mapResponseFailed() throws Exception {
        // GIVEN
        ResponseMapperFilter<TestResponse> underTest = new ResponseMapperFilter<>(TestResponse.class, mapper);
        Mockito.when(response.getContentString()).thenReturn(mapper.writeValueAsString(" "));
        // WHEN
        Future<TestResponse> result = underTest.apply(request, service);
        result.onFailure(e -> {
            assertThat(e.getMessage()).isEqualTo(ErrorCode.INVALID_RESPONSE_FROM_CLIENT.getCode());
            return null;
        });
    }


    private record TestResponse(String value) {
        @JsonCreator
        private TestResponse(@JsonProperty("value") String value) {
            this.value = value;
        }
    }
}

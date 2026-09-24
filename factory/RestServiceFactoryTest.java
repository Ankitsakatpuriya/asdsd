package com.ing.bankguarantees.remote.factory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorDataMapper;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.HttpStatusFilter;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class RestServiceFactoryTest {

    private static final String TEST_STRING = "test";

    private RequestTransformingFilter<String, TestResponse, Request> requestFilter;

    @Mock
    private Request request;
    @Mock
    private Response response;
    @Mock
    private Service<Request, Response> service;
    @Mock
    private HttpStatusFilter httpStatusFilter;
    @InjectMocks
    private RestServiceFactory underTest;
    @Mock
    private Map<ErrorSource, ErrorDataMapper> errorDataMapperMap;


    @BeforeEach
    void initMocks() {
        when(service.apply(any(Request.class))).thenReturn(Future.value(response));
        requestFilter = new RequestTransformingFilter<>(value -> request);
        httpStatusFilter = new HttpStatusFilter(errorDataMapperMap.get(ErrorSource.CHA));
    }


    @Test
    void getRequestReturnsServiceThatRespondsWithError()
            throws InterruptedException, ExecutionException, TimeoutException {

        given(httpStatusFilter.apply(request, service)).willThrow(new RuntimeException(TEST_STRING));

        JavaService<String, TestResponse> testRequest = underTest
                .getRestService(service, requestFilter, TestResponse.class, any());

        CompletableFuture<TestResponse> testResponse = testRequest.apply(TEST_STRING);
        assertThat(testResponse).isCompletedExceptionally();
        TestResponse exceptionMessage = testResponse
                .exceptionally(err -> new TestResponse(err.getMessage())).get(10, TimeUnit.SECONDS);
        assertThat(exceptionMessage.value()).isEqualTo(TEST_STRING);
    }


    private record TestResponse(String value) {

        @JsonCreator
        private TestResponse(@JsonProperty("value") String value) {
            this.value = value;
        }
    }
}

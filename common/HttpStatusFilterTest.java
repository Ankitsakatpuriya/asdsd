package com.ing.bankguarantees.remote.common;


import com.ing.bankguarantees.error.config.ErrorDataMapper;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Await;
import com.twitter.util.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import scala.Option;

import static com.twitter.util.Duration.fromSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpStatusFilterTest {

    private static HttpStatusFilter underTest;

    @Mock
    private Request request;
    @Mock
    private Response response;
    @Mock
    private Service<Request, Response> service;
    @Mock
    private ErrorDataMapper errorDataMapper;

    @BeforeEach
    void initMocks() {
        when(service.apply(request)).thenReturn(Future.value(response));
        underTest = new HttpStatusFilter(errorDataMapper);
    }

    @Test
    void returnServiceResponseForNoErrorWithNoAccessToken() throws Exception {

        Option<String> mediaType = Option.apply("application/json");
        when(response.statusCode()).thenReturn(HttpStatus.OK.value());
        when(response.mediaType()).thenReturn(mediaType);
        Future<Response> result = underTest.apply(request, service);
        Response returnedResponse = Await.result(result, fromSeconds(10));
        assertThat(returnedResponse).isEqualTo(response);
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 201, 303})
    void returnServiceResponseForNoError(int statusCode) throws Exception {

        Option<String> mediaType = Option.apply("application/json");
        when(response.statusCode()).thenReturn(HttpStatus.resolve(statusCode).value());
        when(response.mediaType()).thenReturn(mediaType);
        Future<Response> result = underTest.apply(request, service);
        Response returnedResponse = Await.result(result, fromSeconds(10));
        assertThat(returnedResponse).isEqualTo(response);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 500})
    void returnServiceResponseForError(int statusCode) {
        when(response.statusCode()).thenReturn(HttpStatus.resolve(statusCode).value());
        ErrorItem errorItem = ErrorItem.builder()
                .code("BGOS-06-000")
                .severity("critical")
                .message("BankGuaranteeBEOnlineApplicationAPI encountered an unexpected condition that prevented it from fulfilling the request")
                .source("CSI_HUB_API")
                .externalHttpStatus(HttpStatus.resolve(statusCode))
                .internalHttpStatus(HttpStatus.resolve(statusCode))
                .build();
        when(errorDataMapper.getErrorItemByInternalHttpStatus(response.statusCode())).thenReturn(errorItem);
        when(errorDataMapper.getErrorSource()).thenReturn(ErrorSource.BGOS);
        Future<Response> result = underTest.apply(request, service);
        result.onFailure(e -> {
            assertThat(e.getMessage()).isEqualTo(errorItem.getCode());
            return null;
        });


    }

}

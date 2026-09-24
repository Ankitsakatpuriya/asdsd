package com.ing.bankguarantees.error.config;


import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ErrorDataLoaderTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private ObjectMapper jsonMapper;

    @Test
    void getList() {
        assertThat(new ErrorDataLoader(MAPPER, "errors/%s_errors_test.json").getList(ErrorSource.BGOS)).isNotNull()
                .hasSize(2)
                .contains(ErrorItem.builder()
                        .code("BGOS-01-01")
                        .severity("critical")
                        .message("BankGuaranteeBEOnlineApplicationAPI encountered an unexpected condition that prevented it from fulfilling the request")
                        .externalHttpStatus(HttpStatus.NOT_FOUND)
                        .internalHttpStatus(HttpStatus.NOT_FOUND)
                        .build())
                .contains(ErrorItem.builder()
                        .code("BGOS-01-02")
                        .severity("critical")
                        .message("BankGuaranteeBEOnlineApplicationAPI encountered an unexpected condition that prevented it from fulfilling the request")
                        .externalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .internalHttpStatus(HttpStatus.UNAUTHORIZED)
                        .build());
    }

    @Test
    void getListNoFileFound() {
        ErrorDataLoader errorDataLoader = new ErrorDataLoader(MAPPER, "errors/%s_error.json");
        BgosException bgosException = Assertions.assertThrows(BgosException.class, () -> errorDataLoader
                .getList(ErrorSource.BGOS));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
    }

    @Test
    void getListDeserializationError() {
        ErrorDataLoader errorDataLoader = new ErrorDataLoader(MAPPER, "errors/%s_errors_invalid.json");
        BgosException bgosException = Assertions.assertThrows(BgosException.class, () ->
                errorDataLoader.getList(ErrorSource.BGOS));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
    }


    @Test
    void getListDeserializationIoException() {
        given(jsonMapper.readValue(any(InputStream.class), any(TypeReference.class))).willThrow(JacksonException.class);
        ErrorDataLoader errorDataLoader = new ErrorDataLoader(jsonMapper, "errors/%s_errors_invalid.json");
        BgosException bgosException = Assertions.assertThrows(BgosException.class, () ->
                errorDataLoader.getList(ErrorSource.BGOS));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
    }
}
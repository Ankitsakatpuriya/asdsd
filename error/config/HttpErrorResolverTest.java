package com.ing.bankguarantees.error.config;

import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;


@Slf4j
@ExtendWith(MockitoExtension.class)
class HttpErrorResolverTest {

    @Mock
    private ErrorDataMapper bgosErrorDataMapper;

    @Mock
    private ErrorDataMapper chaErrorDataMapper;

    @Test
    void resolve() {

        given(chaErrorDataMapper.getErrorItemByCode("BGOS-06-000"))
                .willReturn(ErrorItem.builder().code("BGOS-06-000")
                        .severity("critical")
                        .message("BankGuaranteeBEOnlineApplicationAPI encountered an unexpected condition that prevented it from fulfilling the request")
                        .source("CSI_HUB_API")
                        .externalHttpStatus(HttpStatus.BAD_REQUEST)
                        .internalHttpStatus(HttpStatus.BAD_REQUEST)
                        .build());
        assertThat(new HttpErrorResolver(Map.of(ErrorSource.BGOS, bgosErrorDataMapper, ErrorSource.CHA, chaErrorDataMapper))
                .resolve("BGOS-06-000", ErrorSource.CHA))
                .isNotNull()
                .isEqualTo(ErrorItem.builder().code("BGOS-06-000")
                        .severity("critical")
                        .message("BankGuaranteeBEOnlineApplicationAPI encountered an unexpected condition that prevented it from fulfilling the request")
                        .source("CSI_HUB_API")
                        .externalHttpStatus(HttpStatus.BAD_REQUEST)
                        .internalHttpStatus(HttpStatus.BAD_REQUEST)
                        .build());
    }

    @Test
    void resolveFromDefaultErrorMapper() {
        given(bgosErrorDataMapper.getErrorItemByInternalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .willReturn(ErrorItem.builder().code("BGOS-00-500")
                        .severity("critical")
                        .message("Error occurred when consuming third party service.")
                        .source("Internal Server Error")
                        .externalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .internalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
        assertThat(new HttpErrorResolver(Map.of(ErrorSource.BGOS, bgosErrorDataMapper))
                .resolve("BGOS-06-000", ErrorSource.CHA))
                .isNotNull()
                .isEqualTo(ErrorItem.builder().code("BGOS-00-500")
                        .severity("critical")
                        .message("Error occurred when consuming third party service.")
                        .source("Internal Server Error")
                        .externalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .internalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }

}
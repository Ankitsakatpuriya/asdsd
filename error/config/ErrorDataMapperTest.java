package com.ing.bankguarantees.error.config;


import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;


@Slf4j
@ExtendWith(MockitoExtension.class)
class ErrorDataMapperTest {

    @Mock
    private ErrorDataLoader loader;

    @ParameterizedTest(name = "Get error item with {0}")
    @MethodSource(value = "validErrorsMappings")
    void getErrorItemByInternalHttpStatusWithValidMapping(String testCase, HttpStatus requestHttpCode, List<ErrorItem> errors, ErrorItem expectedError) {
        given(loader.getList(ErrorSource.CHA)).willReturn(errors);
        ErrorItem errorItem = new ErrorDataMapper(loader, ErrorSource.CHA).getErrorItemByInternalHttpStatus(requestHttpCode.value());
        assertThat(errorItem).isEqualTo(expectedError);
    }

    @ParameterizedTest(name = "Get error item with {0}")
    @MethodSource(value = "invalidErrorsMappings")
    void getErrorItemByInternalHttpStatusWithInvalidMapping(List<ErrorItem> errors) {
        given(loader.getList(ErrorSource.CHA)).willReturn(errors);
        assertThrows(IllegalArgumentException.class, () -> new ErrorDataMapper(loader, ErrorSource.CHA));
    }


    @ParameterizedTest(name = "Get error item with {0}")
    @MethodSource(value = "validErrorsMappingsByCode")
    void getErrorItemByCodeWithValidMapping(String errorCode, List<ErrorItem> errors, ErrorItem expectedError) {
        given(loader.getList(ErrorSource.CHA)).willReturn(errors);
        ErrorItem errorItem = new ErrorDataMapper(loader, ErrorSource.CHA).getErrorItemByCode(errorCode);
        assertThat(errorItem).isEqualTo(expectedError);
    }

    @Test
    void getErrorItemByCodeWithNull() {
        given(loader.getList(ErrorSource.CHA)).willReturn(getValidErrorItems());
        ErrorItem errorItem = new ErrorDataMapper(loader, ErrorSource.CHA).getErrorItemByCode("BGOS-06-001");
        assertThat(errorItem.getInternalHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getErrorItemByCodeWithInternalStatusNull() {
        given(loader.getList(ErrorSource.CHA)).willReturn(getNullInternalStatusErrorItems());
        assertThrows(IllegalArgumentException.class, () -> new ErrorDataMapper(loader, ErrorSource.CHA));
    }

    @Test
    void getValidErrorSource() {
        given(loader.getList(ErrorSource.CHA)).willReturn(getValidErrorItems());
        ErrorSource errorSource = new ErrorDataMapper(loader, ErrorSource.CHA).getErrorSource();
        assertThat(errorSource).isEqualTo(ErrorSource.CHA);
    }


    private static Stream<Arguments> validErrorsMappings() {
        return Stream.of(
                Arguments.of("map bad response", HttpStatus.BAD_REQUEST,
                        getValidErrorItems(),
                        ErrorItem.builder().code("BGOS-06-000")
                                .severity("critical")
                                .message("BankGuaranteeBEOnlineApplicationAPI encountered an unexpected condition that prevented it from fulfilling the request")
                                .source("CSI_HUB_API")
                                .externalHttpStatus(HttpStatus.BAD_REQUEST)
                                .internalHttpStatus(HttpStatus.BAD_REQUEST).build()
                ),
                Arguments.of("returns default mapping - 500", HttpStatus.FORBIDDEN,
                        getValidErrorItems(),
                        ErrorItem.builder().code("BGOS-06-008")
                                .severity("critical")
                                .message("BankGuaranteeBEOnlineApplicationAPI encountered an unexpected condition that prevented it from fulfilling the request")
                                .source("CSI_HUB_API")
                                .externalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                .internalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR).build()
                ));
    }

    private static Stream<Arguments> invalidErrorsMappings() {
        return Stream.of(
                Arguments.of(asList(ErrorItem.builder().code("BGOS-06-000")
                                .severity("critical")
                                .message("BankGuaranteeBEOnlineApplicationAPI encountered an unexpected condition that prevented it from fulfilling the request")
                                .source("CSI_HUB_API")
                                .externalHttpStatus(HttpStatus.BAD_REQUEST)
                                .internalHttpStatus(HttpStatus.BAD_REQUEST)
                                .build(),
                        ErrorItem.builder().code("BGOS-06-008")
                                .severity("critical")
                                .message("BankGuaranteeBEOnlineApplicationAPI encountered an unexpected condition that prevented it from fulfilling the request")
                                .source("CSI_HUB_API")
                                .externalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                .internalHttpStatus(HttpStatus.UNAUTHORIZED)
                                .build()
                )),
                Arguments.of(asList(ErrorItem.builder().code("BGOS-06-000")
                                        .severity("critical")
                                        .message("BankGuaranteeBEOnlineApplicationAPI encountered an unexpected condition that prevented it from fulfilling the request")
                                        .source("CSI_HUB_API")
                                        .externalHttpStatus(HttpStatus.BAD_REQUEST)
                                        .internalHttpStatus(HttpStatus.BAD_REQUEST)
                                        .build(),
                                ErrorItem.builder().code("BGOS-06-008")
                                        .severity("critical")
                                        .message("BankGuaranteeBEOnlineApplicationAPI encountered an unexpected condition that prevented it from fulfilling the request")
                                        .source("CSI_HUB_API")
                                        .externalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .internalHttpStatus(HttpStatus.BAD_REQUEST)
                                        .build()
                        )
                ));
    }

    private static Stream<Arguments> validErrorsMappingsByCode() {
        return Stream.of(
                Arguments.of("BGOS-06-000",
                        getValidErrorItems(),
                        ErrorItem.builder().code("BGOS-06-000")
                                .severity("critical")
                                .message("BankGuaranteeBEOnlineApplicationAPI encountered an unexpected condition that prevented it from fulfilling the request")
                                .source("CSI_HUB_API")
                                .externalHttpStatus(HttpStatus.BAD_REQUEST)
                                .internalHttpStatus(HttpStatus.BAD_REQUEST)
                                .build()
                ),
                Arguments.of("BGOS-06-008",
                        getValidErrorItems(),
                        ErrorItem.builder().code("BGOS-06-008")
                                .severity("critical")
                                .message("BankGuaranteeBEOnlineApplicationAPI encountered an unexpected condition that prevented it from fulfilling the request")
                                .source("CSI_HUB_API")
                                .externalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                .internalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                .build()
                ));
    }

    private static List<ErrorItem> getValidErrorItems() {
        return asList(ErrorItem.builder().code("BGOS-06-000")
                        .severity("critical")
                        .message("BankGuaranteeBEOnlineApplicationAPI encountered an unexpected condition that prevented it from fulfilling the request")
                        .source("CSI_HUB_API")
                        .externalHttpStatus(HttpStatus.BAD_REQUEST)
                        .internalHttpStatus(HttpStatus.BAD_REQUEST)
                        .build(),
                ErrorItem.builder().code("BGOS-06-008")
                        .severity("critical")
                        .message("BankGuaranteeBEOnlineApplicationAPI encountered an unexpected condition that prevented it from fulfilling the request")
                        .source("CSI_HUB_API")
                        .externalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .internalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }

    private static List<ErrorItem> getNullInternalStatusErrorItems() {
        return Collections.singletonList(ErrorItem.builder().code("BGOS-06-000")
                .severity("critical")
                .message("BankGuaranteeBEOnlineApplicationAPI encountered an unexpected condition that prevented it from fulfilling the request")
                .source("CSI_HUB_API")
                .externalHttpStatus(HttpStatus.BAD_REQUEST)
                .internalHttpStatus(null)
                .build());

    }

}
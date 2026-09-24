package com.ing.bankguarantees.error.config.modeljson;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpStatusDeserializerTest {

    private static Stream<Arguments> jsonParameters() {
        return Stream.of(
                Arguments.of("{\"code\":\"BGOS-00-004\",\"externalHttpStatus\":500,\"internalHttpStatus\":401}",
                        HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.UNAUTHORIZED),
                Arguments.of("{\"code\":\"BGOS-00-004\",\"externalHttpStatus\":500,\"internalHttpStatus\":null}",
                        HttpStatus.INTERNAL_SERVER_ERROR, null)
        );

    }

    @ParameterizedTest
    @MethodSource("jsonParameters")
    void convert(String inputJson, HttpStatus externalHttpStatus, HttpStatus internalHttpStatus) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(HttpStatus.class, new HttpStatusDeserializer());
        ObjectMapper mapper = JsonMapper.builder().addModule(module).build();
        ErrorItem readValue = mapper.readValue(inputJson, ErrorItem.class);
        assertThat(readValue).isNotNull();
        assertThat(readValue.getCode()).isEqualTo("BGOS-00-004");
        assertThat(readValue.getExternalHttpStatus()).isEqualTo(externalHttpStatus);
        assertThat(readValue.getInternalHttpStatus()).isEqualTo(internalHttpStatus);
    }

    @Test
    public void convert() {
        String json = "{\"code\":\"BGOS-00-004\",\"externalHttpStatus\":500,\"internalHttpStatus\":9999}";
        SimpleModule module = new SimpleModule();
        module.addDeserializer(HttpStatus.class, new HttpStatusDeserializer());
        ObjectMapper mapper = JsonMapper.builder().addModule(module).build();
        assertThrows(JacksonException.class, () -> mapper.readValue(json, ErrorItem.class));
    }


}
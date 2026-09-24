package com.ing.bankguarantees.error.config.modeljson;


import org.springframework.http.HttpStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class HttpStatusDeserializer extends ValueDeserializer<HttpStatus> {


    @Override
    public HttpStatus deserialize(JsonParser p, DeserializationContext ctxt) {
        int code = p.getIntValue();
        return HttpStatus.valueOf(code);
    }
}
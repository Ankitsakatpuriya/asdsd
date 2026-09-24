package com.ing.bankguarantees.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.module.scala.DefaultScalaModule;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Configuration
public class JacksonConfiguration {

    /**
     * @return ObjectMapper for converting the response to corresponding class type
     * for all our interactions with different apis.
     */
    @Bean
    public static ObjectMapper createDefaultMapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(NON_NULL).withContentInclusion(NON_NULL))
                .addModule(new DefaultScalaModule())
                .build();
    }
}

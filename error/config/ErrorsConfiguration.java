package com.ing.bankguarantees.error.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class configuring beans for error mappings
 */
@Configuration
public class ErrorsConfiguration {

    /**
     * Create a {@link Map} bean - map error mapper by source system
     *
     * @param errorDataLoader errors Loader of json error files
     * @return a {@link ErrorDataMapper} object
     */
    @Bean
    public Map<ErrorSource, ErrorDataMapper> errorDataMapperMap(ErrorDataLoader errorDataLoader) {
        Map<ErrorSource, ErrorDataMapper> mapperMap = Stream.of(ErrorSource.values())
                .map(errorSource -> new ErrorDataMapper(errorDataLoader, errorSource))
                .collect(Collectors.toMap(ErrorDataMapper::getErrorSource, Function.identity()));
        if (!mapperMap.containsKey(ErrorSource.BGOS)) {
            throw new IllegalArgumentException("Default error mapper not configured");
        }
        return mapperMap;
    }
}

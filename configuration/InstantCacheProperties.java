package com.ing.bankguarantees.configuration;

import com.ing.bankguarantees.remote.utils.YamlPropertySourceFactory;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "bgos.cache")
@PropertySource(value = "classpath:props/applicationCacheConfiguration.yml", factory = YamlPropertySourceFactory.class)
public class InstantCacheProperties {
    @NotNull
    private String defaultConfiguration;
    @NotNull
    private List<CacheProperties> caches;

    @Getter
    @Setter
    public static class CacheProperties {
        @NotNull
        private String name;
        @NotNull
        private String configuration;
    }
}

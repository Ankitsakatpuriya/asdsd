package com.ing.bankguarantees.remote.rest.namescreening;

import com.ing.bankguarantees.remote.utils.YamlPropertySourceFactory;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

/**
 * Properties for name screening api
 */
@Getter
@Setter
@Configuration
@Validated
@ConfigurationProperties("remote.rest.name-screening")
@PropertySource(value = "classpath:props/beRemoteRestIntegrationsInput.yml", factory = YamlPropertySourceFactory.class)
public class NameScreeningProperties {

    @NotNull
    private String partyType;
    @NotNull
    private String businessUnit;
    @NotNull
    private String actorCountry;
    @NotNull
    private String screeningType;
    @NotNull
    private String alertGenerate;
}


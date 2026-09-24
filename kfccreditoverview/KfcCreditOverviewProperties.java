package com.ing.bankguarantees.remote.rest.kfccreditoverview;

import com.ing.bankguarantees.remote.utils.YamlPropertySourceFactory;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

/**
 * Properties for credit risk api
 */
@Getter
@Setter
@Configuration
@Validated
@ConfigurationProperties("remote.rest.kfc-credit-overview")
@PropertySource(value = "classpath:props/beRemoteRestIntegrationsInput.yml", factory = YamlPropertySourceFactory.class)
public class KfcCreditOverviewProperties {

    @NotNull
    private String involvePartyType;

    @NotNull
    private String employeeIdType;

    @NotNull
    private String employeeIdValue;

    @NotNull
    private Integer level;


}


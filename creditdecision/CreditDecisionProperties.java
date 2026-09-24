package com.ing.bankguarantees.remote.rest.creditdecision;

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
@ConfigurationProperties("remote.rest.credit-risk")
@PropertySource(value = "classpath:props/beRemoteRestIntegrationsInput.yml", factory = YamlPropertySourceFactory.class)
public class CreditDecisionProperties {

    @NotNull
    private String requestType;
    @NotNull
    private String defaultBranch;
    @NotNull
    private String eurCurrency;
    @NotNull
    private Integer csiLength;
    @NotNull
    private String privateUse;
    @NotNull
    private String productType;
    @NotNull
    private String productNatureType;
    @NotNull
    private String organisationPartnershipType;
    @NotNull
    private String individualPartnershipType;
    @NotNull
    private Integer legalEntityWorkStability;
    @NotNull
    private Integer selfEmployedWorkStability;
    @NotNull
    private String requestChannel;
    @NotNull
    private Integer capitalPaymentFrequency;
    @NotNull
    private String organisationIntervenientType;
    @NotNull
    private String individualIntervenientType;

    @NotNull
    private Integer greenPdlBusinessLineDurationInMonths;

}


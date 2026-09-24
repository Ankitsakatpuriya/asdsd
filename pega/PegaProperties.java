package com.ing.bankguarantees.remote.rest.pega;

import com.ing.bankguarantees.remote.utils.YamlPropertySourceFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties("remote.rest.pega")
@PropertySource(value = "classpath:props/beRemoteRestIntegrationsInput.yml", factory = YamlPropertySourceFactory.class)
public class PegaProperties {

    @NotBlank
    private String caseTypeId;

    @NotBlank
    private String processId;

    @NotBlank
    private String branchName;

    @NotBlank
    private String indirectGuarantee;

    @NotBlank
    private String typeUrgent;

    @NotBlank
    private String channelName;

    @NotBlank
    private String typeStandard;

    @NotBlank
    private String pyLabel;

    @NotBlank
    private String requestType;

    @NotBlank
    private String requestTypeAmend;


    @NotBlank
    private String currency;

    @NotNull
    private Boolean crossBorder;

    @NotBlank
    private String typeOfSubmission;

    @NotBlank
    private String team;

    @NotBlank
    private String orfAgreementReceived;


}

package com.ing.bankguarantees.remote.rest.dar;

import com.ing.bankguarantees.remote.utils.YamlPropertySourceFactory;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties("remote.rest.dar")
@PropertySource(value = "classpath:props/beRemoteRestIntegrationsInput.yml", factory = YamlPropertySourceFactory.class)
public class DarProperties {

    @NotNull
    private String businessLine;

    @NotNull
    private String tribe;

    @NotNull
    private Integer daysToAdd;

    @NotNull
    private String documentSignCallback;

    @NotNull
    private String documentSignIntermediateCallback;

    @NotNull
    private String signNamePrefix;

    @NotNull
    private String documentUriPrefix;

    @NotNull
    private String frontendCallbackUrl;

    @NotNull
    private String firstConfirmationLink;

    @NotNull
    private String secondConfirmationLink;

    @NotNull
    private String cancelUrl;

    @NotNull
    private String closeUrl;

    @NotNull
    private String expiryMessageNl;

    @NotNull
    private String expiryMessageFr;

    @NotNull
    private String expiryMessageEn;


}

package com.ing.bankguarantees.remote.rest.pamqualification;

import com.ing.bankguarantees.remote.utils.YamlPropertySourceFactory;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Configuration
@Validated
@ConfigurationProperties("remote.rest.pam-qualification-checks")
@PropertySource(value = "classpath:props/beRemoteRestIntegrationsInput.yml", factory = YamlPropertySourceFactory.class)
public class PamQualificationProperties {

    @NotNull
    private String operation;

    @NotNull
    private String ingEntity;

    @NotNull
    private String uuidCode;


}

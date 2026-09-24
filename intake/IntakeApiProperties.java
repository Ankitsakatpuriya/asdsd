package com.ing.bankguarantees.remote.rest.intake;

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
@ConfigurationProperties("remote.rest.intake")
@PropertySource(value = "classpath:props/beRemoteRestIntegrationsInput.yml", factory = YamlPropertySourceFactory.class)
public class IntakeApiProperties {

    @NotNull
    private String team;

    @NotNull
    private String otherDocumentsNeeded;

    @NotNull
    private String ingChannel;

    @NotNull
    private String orfAgreementReceived;

    @NotNull
    private String indirectGuarantee;

    @NotNull
    private String tiTeam;

    @NotNull
    private String customerSegment;

    @NotNull
    private String garCover;
}

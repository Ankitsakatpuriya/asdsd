package com.ing.bankguarantees.remote.rest.connectdot;

import com.ing.bankguarantees.remote.utils.YamlPropertySourceFactory;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Data
@Validated
@Configuration
@ConfigurationProperties("remote.rest.connect-dot")
@PropertySource(value = "classpath:props/beRemoteRestIntegrationsInput.yml", factory = YamlPropertySourceFactory.class)
public class ConnectDotProperties {

    @NotEmpty
    private String costCenter;
    @NotEmpty
    private String entityCode;
    @NotEmpty
    private String initiatingCI;
    @NotEmpty
    private String recipientType;
    @NotEmpty
    private String recipientPartyType;

    @NotEmpty
    private String destinationOutput;
    @NotEmpty
    private String destinationType;

    @NotEmpty
    private String destinationOutputFinal;

    @NotEmpty
    private String channelsImportance;
    @NotEmpty
    private String periodicityForRentalEn;
    @NotEmpty
    private String periodicityForRentalFr;
    @NotEmpty
    private String periodicityForRentalNl;
    @NotEmpty
    private String periodicityForOthersEn;
    @NotEmpty
    private String periodicityForOthersFr;
    @NotEmpty
    private String periodicityForOthersNl;

    @NotEmpty
    private String ratePerYear;
    @NotEmpty
    private String minimumPerRecord;
    @NotEmpty
    private String minimumPerRecordForDck;

    @NotEmpty
    private String firstIngSignerName;

    @NotEmpty
    private String secondIngSignerName;

    @NotEmpty
    private String publicationBelgianGazetteNL;

    @NotEmpty
    private String publicationBelgianGazetteFR;

    private Map<String, List<String>> typeAmount;

}

package com.ing.bankguarantees.remote.rest.amsklc.booking;

import com.ing.bankguarantees.remote.utils.YamlPropertySourceFactory;
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
@ConfigurationProperties("remote.rest.booking")
@PropertySource(value = "classpath:props/beRemoteRestIntegrationsInput.yml", factory = YamlPropertySourceFactory.class)
public class BookingApiProperties {

    private String dataSource;
    private String eventIdentifierType;
    private Integer mutationCode;
    private Integer createMutationCode;
    private Integer statusLotAvoi;
    private Integer createStatusLotAvoi;
    private Integer creationLinkCode;
    private String rangeNumber;
    private String agreementIdentifierType;
    private String agreementIdentifierValue;
    private Integer productCode;
}


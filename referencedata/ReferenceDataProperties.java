package com.ing.bankguarantees.remote.rest.referencedata;

import com.ing.bankguarantees.remote.utils.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties("remote.rest.reference-data.multilingual")
@PropertySource(value = "classpath:props/beRemoteRestIntegrationsInput.yml", factory = YamlPropertySourceFactory.class)
public class ReferenceDataProperties {

    private String tableDistributionName;
    private List<String> language;
    private List<String> columns;

}

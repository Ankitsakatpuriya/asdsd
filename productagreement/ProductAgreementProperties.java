package com.ing.bankguarantees.remote.rest.productagreement;

import com.ing.bankguarantees.remote.utils.YamlPropertySourceFactory;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Getter
@Setter
@Configuration
@Validated
@ConfigurationProperties("remote.rest.product-agreement")
@PropertySource(value = "classpath:props/beRemoteRestIntegrationsInput.yml", factory = YamlPropertySourceFactory.class)
public class ProductAgreementProperties {

    @NotEmpty
    private Set<String> productTypes;

    @NotEmpty
    private Set<String> currentAccountProductTypes;

    @NotEmpty
    private Set<String> creditLineProductTypes;

    @NotEmpty
    private String productStatus;


}

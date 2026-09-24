package com.ing.bankguarantees.remote.kafka.datalakeevent;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "data-lake-event")
public class DataLakeKafkaProperties {
    @NotNull
    private String topic;

    @NotNull
    private String sharedSecret;

    private String clientId;

}


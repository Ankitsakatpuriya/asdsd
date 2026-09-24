package com.ing.bankguarantees.configuration;

import com.ing.bodega.ReleaseNotificationEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Map;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;


@Slf4j
@Configuration
public class TiFeedbackKafkaConfiguration {

    private static final String Ti_FEEDBACK_CONSUMER_GROUP = "BankGuaranteeOnlineWebServiceTiConsumersGroup";
    private static final String TI_CLIENT_ID = "BankGuaranteeOnlineWebServiceTiConsumer";

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;


    @Bean
    public ConsumerFactory<String, ReleaseNotificationEvent> tiPlusConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();
        consumerProperties.put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, Ti_FEEDBACK_CONSUMER_GROUP);
        consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, TI_CLIENT_ID);
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReleaseNotificationEvent> tiPlusFeedbackListenerContainerFactory(ConsumerFactory<String, ReleaseNotificationEvent> tiPlusConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, ReleaseNotificationEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(tiPlusConsumerFactory);
        return factory;
    }
}
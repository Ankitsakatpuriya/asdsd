package com.ing.bankguarantees.configuration;

import com.ing.tpa.esuite.notification.external.feedback.ExternalFeedbackEvent;
import com.ing.tpa.esuite.notificationapi.domain.EngagementSuiteNotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Map;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;


@Slf4j
@EnableKafka
@Configuration
public class KafkaEngagementSuiteConfiguration {

    private static final String NOTIFICATION_EVENT_CONSUMER_GROUP = "bank-guarantee-online-web-es-v2-consumers";

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;


    @Bean
    public Producer<String, EngagementSuiteNotificationEvent> producerFactory(KafkaProperties kafkaProperties) {

        Map<String, Object> props = kafkaProperties.buildProducerProperties();
        return new KafkaProducer<>(props);
    }

    @Bean
    public ConsumerFactory<String, ExternalFeedbackEvent> notificationConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();
        consumerProperties.put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, NOTIFICATION_EVENT_CONSUMER_GROUP);
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ExternalFeedbackEvent> notificationListenerContainerFactory(
            ConsumerFactory<String, ExternalFeedbackEvent> notificationConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, ExternalFeedbackEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationConsumerFactory);
        return factory;
    }


}

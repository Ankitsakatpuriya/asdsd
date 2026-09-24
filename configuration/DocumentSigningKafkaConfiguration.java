package com.ing.bankguarantees.configuration;

import com.ing.docsign.CallbackEvent;
import com.ing.docsign.callback.DetailedCallbackEvent;
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
public class DocumentSigningKafkaConfiguration {

    private static final String DOCSIGN_EVENT_CONSUMER_GROUP = "BGOS-Callback-Group";

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;


    @Bean
    public ConsumerFactory<String, CallbackEvent> docsignConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();
        consumerProperties.put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, DOCSIGN_EVENT_CONSUMER_GROUP);
        consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "BankGuaranteeOnlineWebServiceDocSign");
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }

    @Bean
    public ConsumerFactory<String, DetailedCallbackEvent> docsignIntermediateConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();
        consumerProperties.put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, DOCSIGN_EVENT_CONSUMER_GROUP);
        consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "BankGuaranteeOnlineWebServiceDocSignIntermediate");
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CallbackEvent> documentSigningListenerContainerFactory(ConsumerFactory<String, CallbackEvent> docsignConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, CallbackEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(docsignConsumerFactory);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DetailedCallbackEvent> docSignIntermediateCallbackContainerFactory(ConsumerFactory<String, DetailedCallbackEvent> docsignIntermediateConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, DetailedCallbackEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(docsignIntermediateConsumerFactory);
        return factory;
    }
}
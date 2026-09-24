package com.ing.bankguarantees.configuration;

import com.ing.apisdk.toolkit.connectivity.kafka.avro.serde.EncryptingKafkaPayloadAvroSerializer;
import com.ing.apisdk.toolkit.connectivity.kafka.avro.serde.EncryptionAwareSerDeConfig;
import com.ing.bankguarantees.avro.BankGuaranteesDataLakeEvent;
import com.ing.bankguarantees.remote.kafka.datalakeevent.DataLakeKafkaProperties;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import java.util.Map;

@Slf4j
@Configuration
@EnableKafka
@EnableConfigurationProperties
@RequiredArgsConstructor
public class DataLakeEventProducerConfiguration {

    private final DataLakeKafkaProperties dataLakeKafkaProperties;

    @Bean(name = "dataLakeProducer")
    public Producer<String, BankGuaranteesDataLakeEvent> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildProducerProperties();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EncryptingKafkaPayloadAvroSerializer.class);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, dataLakeKafkaProperties.getClientId());
        props.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, false);
        props.put(EncryptionAwareSerDeConfig.SHARED_SECRET_CONFIG, dataLakeKafkaProperties.getSharedSecret());
        props.put(AbstractKafkaSchemaSerDeConfig.USE_LATEST_VERSION, true);
        return new KafkaProducer<>(props);
    }
}

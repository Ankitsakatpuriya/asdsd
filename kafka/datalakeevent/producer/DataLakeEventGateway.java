package com.ing.bankguarantees.remote.kafka.datalakeevent.producer;

import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.avro.BankGuaranteesDataLakeEvent;
import com.ing.bankguarantees.remote.kafka.datalakeevent.DataLakeKafkaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataLakeEventGateway {

    private final Producer<String, BankGuaranteesDataLakeEvent> dataLakeProducer;
    private final DataLakeKafkaProperties dataLakeEventProperties;

    public CompletableFuture<Boolean> send(BankGuaranteesDataLakeEvent event) {

        String topic = dataLakeEventProperties.getTopic();
        String eventName = event.getHeader().getEventName().name();

        log.info("DataLakeEventGateway [send] Sending event to the topic {} with eventName {}", topic, eventName);
        log.info(C3LogMarker.marker, "DataLakeEventGateway [send] Sending event to the topic {} with eventName [{}] and data {}", topic, eventName, event);
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            dataLakeProducer.send(new ProducerRecord<>(topic, event),
                    (RecordMetadata metadata, Exception exception) -> {
                        if (exception == null) {
                            log.info("DataLakeEventGateway [send] Kafka event successfully sent to the topic {} with eventName [{}] ", topic, eventName);
                            future.complete(true);
                        } else {
                            log.error("DataLakeEventGateway [send] Kafka Exception send error for event [{}]: {}", eventName, exception.getMessage(), exception);
                            future.completeExceptionally(exception);

                        }
                    });
        } catch (Exception exception) {
            log.error("DataLakeEventGateway [send] Kafka Exception catch [{}]: {}", eventName, exception.getMessage(), exception);
            future.completeExceptionally(exception);
        }

        return future;
    }
}

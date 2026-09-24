package com.ing.bankguarantees.remote.kafka.engagementsuite;

import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.NotificationEvent;
import com.ing.bankguarantees.remote.kafka.engagementsuite.producer.callback.NotificationFutureCallback;
import com.ing.bankguarantees.remote.kafka.engagementsuite.transformer.NotificationEventTransformer;
import com.ing.tpa.esuite.notificationapi.domain.EngagementSuiteNotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;


/**
 *
 */
@Slf4j
@Service
public class EngagementSuiteGateway {

    private static final String KAFKA_ENCRYPTION_KEY = "P31464/SecretReference/kafka-encryption-key";

    private static final String DECRYPTION_KEY_REFERENCE_HEADER = "decryption-key-reference";

    public final Producer<String, EngagementSuiteNotificationEvent> producerFactory;
    private final NotificationEventTransformer notificationEventTransformer;
    private final ExecutorService executorService;

    public EngagementSuiteGateway(Producer<String, EngagementSuiteNotificationEvent> producerFactory,
                                  NotificationEventTransformer notificationEventTransformer,
                                  @Qualifier("workStealingPool") ExecutorService executorService) {
        this.producerFactory = producerFactory;
        this.notificationEventTransformer = notificationEventTransformer;
        this.executorService = executorService;
    }

    public void send(NotificationEvent event, String topic, NotificationFutureCallback callback) {
        CompletableFuture.runAsync(() -> {

            log.info("EngagementSuiteGateway [send]  Sending event to the topic {} with communication {} for applicationId {} and",
                    topic, event.getBody().getCommunication(), event.getHeader().getMetadata().get("applicationId"));
            try {
                EngagementSuiteNotificationEvent engagementSuiteNotificationEvent = notificationEventTransformer.transform(event);

                log.info(C3LogMarker.marker, "EngagementSuiteGateway [send]  Sending event to the topic {} with communication {}, data {}",
                        topic, event.getBody().getCommunication(), engagementSuiteNotificationEvent);

                ProducerRecord<String, EngagementSuiteNotificationEvent> producerRecord = new ProducerRecord<>(topic, engagementSuiteNotificationEvent);
                producerRecord.headers().add(DECRYPTION_KEY_REFERENCE_HEADER, KAFKA_ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8));

                producerFactory.send(producerRecord, (response, exception) -> {
                    if (ObjectUtils.isNotEmpty(exception))
                        callback.onFailure(exception);
                    else
                        callback.onSuccess();
                });

            } catch (Exception exception) {
                callback.onFailure(exception);
            }
        }, executorService);
    }

}

package com.ing.bankguarantees.remote.kafka.engagementsuite.producer.callback;

import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.logger.ExceptionLogger;
import com.ing.bankguarantees.remote.kafka.engagementsuite.NotificationPropertiesDetails;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.NotificationEvent;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationEventParams.APPLICATION_ID;

@Slf4j
@Builder
@RequiredArgsConstructor
public class EmailNotificationCallback implements NotificationFutureCallback {

    private final NotificationEvent notificationEvent;
    private final NotificationPropertiesDetails notificationPropertiesDetails;


    @Override
    public void onFailure(@NotNull Throwable throwable) {
        Map<String, String> metadataParams = notificationEvent.getHeader().getMetadata();
        String requestId = metadataParams.get(APPLICATION_ID);

        ExceptionLogger.error(throwable, C3LogMarker.marker, """
                        EmailNotificationCallback [onFailure] Engagement Suite: Failed to send kafka event to the \
                        topic {} with communication {}, and error message {} for applicationId {}""",
                notificationPropertiesDetails.getTopic(), notificationEvent.getBody().getCommunication(),
                throwable.getMessage(), requestId);

    }

    @Override
    public void onSuccess() {

        Map<String, String> metadataParams = notificationEvent.getHeader().getMetadata();
        log.info("EmailNotificationCallback [onSuccess] Engagement Suite: Kafka event successfully" +
                        " sent to topic {}, communication {} for application id {}", notificationPropertiesDetails.getTopic(),
                notificationEvent.getBody().getCommunication(), metadataParams.get(APPLICATION_ID));
    }
}
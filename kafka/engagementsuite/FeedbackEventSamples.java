package com.ing.bankguarantees.remote.kafka.engagementsuite;

import com.ing.bankguarantees.remote.kafka.engagementsuite.model.FeedbackEvent;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.FeedbackEventBody;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.FeedbackReason;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.Header;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.utils.TracingHelper;
import com.ing.tpa.esuite.notification.EsKafkaHeaderNames;
import com.ing.tpa.esuite.notification.external.feedback.Error;
import com.ing.tpa.esuite.notification.external.feedback.ExternalFeedbackEvent;
import com.ing.tpa.esuite.notification.external.feedback.NotificationFeedback;
import com.ing.tpa.esuite.notification.feedback.FeedbackStatus;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class FeedbackEventSamples {

    private static final String FEEDBACK_EVENT_SUCCESSFUL_JSON = "BGA/BGF/ExternalFeedbackEventSuccessful.json";

    public static final String APPLICATION_ID = "applicationId";
    public static final String COMMUNICATION = "communication";
    private static final String TEMPLATE_REFERENCE = "templateReference";
    private static final String TRACE_ID = "traceId";
    private static final String ID = "123";
    private static final String STATUS = "status";
    private static final String RECIPIENT_ADDRESS = "recipientAddress";
    private static final String PROFILE_ID = "profileId";
    private static final String LOCALE = "locale";
    private static final String CHANNEL = "channel";
    private static final String MESSAGE_ID = "messageId";
    public static final String SPAN_ID = "spanId";
    public static final String PARENT_ID = "parentId";
    public static final String SESSION_ID = "sessionId";
    public static final String STORAGE_IN_XCP = "storageInXCP";
    private static final long CREATED_AT = 123456789L;


    @AllArgsConstructor
    @Getter
    public enum DeliveryStatus {
        SUCCESSFULLY_DELIVERED(FeedbackStatus.DELIVERED_TO_PROVIDER, null, null),
        NOT_DELIVERED(FeedbackStatus.NOT_DELIVERED_TO_PROVIDER, "Address not valid", "Address not valid");
        private final String feedbackStatus;
        private final String errorCode;
        private final String errorReason;
    }


    public static FeedbackEvent getFeedbackEvent(String communication, String deliveryStatus) {

        HashMap<String, String> metadata = new HashMap<>();
        metadata.put(APPLICATION_ID, APPLICATION_ID);
        metadata.put(STORAGE_IN_XCP, String.valueOf(Boolean.TRUE));

        return FeedbackEvent.builder()
                .header(Header.builder()
                        .created(CREATED_AT)
                        .id(ID)
                        .traceId(TRACE_ID)
                        .metadata(metadata)
                        .build())
                .body(FeedbackEventBody.builder()
                        .notificationCreated(CREATED_AT)
                        .templateReference(TEMPLATE_REFERENCE)
                        .status(STATUS)
                        .recipientAddress(RECIPIENT_ADDRESS)
                        .profileId(PROFILE_ID)
                        .locale(LOCALE)
                        .channel(CHANNEL)
                        .messageId(MESSAGE_ID)
                        .reason(FeedbackReason.builder()
                                .code(deliveryStatus)
                                .description(deliveryStatus)
                                .build())
                        .communication(communication)
                        .build())
                .build();
    }


    public static ExternalFeedbackEvent getExternalFeedbackEvent(String communication, String deliveryStatus, String errorCode,
                                                                 String errorMessage) {

        ExternalFeedbackEvent externalFeedbackEvent = MockHelper.externalFeedbackEvent(FEEDBACK_EVENT_SUCCESSFUL_JSON);
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put(APPLICATION_ID, APPLICATION_ID);
        metadata.put(COMMUNICATION, communication);
        Error error = null;
        if (errorCode != null) {
            error = Error.newBuilder()
                    .setCode(errorCode)
                    .setMessage(errorMessage)
                    .setSeverity("Critical")
                    .build();
        }
        SpanContext ingSpanContext = TracingHelper.getINGSpanContext();
        metadata.put(TRACE_ID, ingSpanContext.getTraceId());
        metadata.put(SPAN_ID, ingSpanContext.getSpanId());
        metadata.put(PARENT_ID, ingSpanContext.getTraceId());
        metadata.put(SESSION_ID, UUID.randomUUID().toString());
        externalFeedbackEvent.setNotificationMetadata(metadata);

        NotificationFeedback notificationFeedback = NotificationFeedback.newBuilder()
                .setStatus(deliveryStatus)
                .setError(error)
                .setTimestamp(Instant.now())
                .build();
        externalFeedbackEvent.setNotificationFeedback(notificationFeedback);
        return externalFeedbackEvent;
    }

    public static Message<ExternalFeedbackEvent> getExternalFeedbackEventMessage(String communication, String deliveryStatus,
                                                                                 String errorCode, String errorMessage) {
        return new Message<>() {
            @Override
            public @NotNull ExternalFeedbackEvent getPayload() {
                return getExternalFeedbackEvent(communication, deliveryStatus, errorCode, errorMessage);
            }

            @Override
            public @NotNull MessageHeaders getHeaders() {
                Map<String, Object> headerMap = new HashMap<>();
                headerMap.put(EsKafkaHeaderNames.PURPOSE_ID, "P31464".getBytes(StandardCharsets.UTF_8));
                return new MessageHeaders(headerMap);
            }
        };

    }

    public static Message<ExternalFeedbackEvent> getExternalFeedbackEventMessage(String communication, String deliveryStatus) {

        return getExternalFeedbackEventMessage(communication, deliveryStatus, null, null);
    }

}

package com.ing.bankguarantees.remote.kafka.engagementsuite.consumer;

import com.ing.bankguarantees.remote.kafka.engagementsuite.consumer.feedbackhandler.FeedbackHandler;
import com.ing.bankguarantees.remote.kafka.engagementsuite.consumer.feedbackhandler.FeedbackHandlerFactory;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.FeedbackEvent;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.SupportedCommunication;
import com.ing.bankguarantees.remote.kafka.engagementsuite.transformer.FeedbackEventTransformer;
import com.ing.bankguarantees.utils.TracingHelper;
import com.ing.tpa.esuite.notification.EsKafkaHeaderNames;
import com.ing.tpa.esuite.notification.external.feedback.ExternalFeedbackEvent;
import com.ing.tpa.esuite.notification.feedback.FeedbackStatus;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationEventParams.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventFeedbackConsumer {

    private static final String NOTIFICATION_EVENT_FEEDBACK_TOPIC = "CSO_BE_FeedbackEvent";
    private static final String PURPOSE_CODE = "P31464";
    private static final String TRACER_NAME = "email_callback_tracer";
    private static final String PROCESS_NAME = "process_email_feedback";

    private final FeedbackHandlerFactory feedbackHandlerFactory;


    @KafkaListener(topics = NOTIFICATION_EVENT_FEEDBACK_TOPIC, containerFactory = "notificationListenerContainerFactory")
    public void listen(Message<ExternalFeedbackEvent> externalFeedbackEvent) {
        Object purposeIdObject = ObjectUtils.isNotEmpty(externalFeedbackEvent) ? externalFeedbackEvent.getHeaders().get(EsKafkaHeaderNames.PURPOSE_ID) : null;
        if (ObjectUtils.isNotEmpty(purposeIdObject)) {
            String purposeId = new String((byte[]) purposeIdObject);
            ExternalFeedbackEvent externalFeedbackEventPayload = externalFeedbackEvent.getPayload();
            if (PURPOSE_CODE.equals(purposeId)) {
                Map<String, String> metadataParams = externalFeedbackEventPayload.getNotificationMetadata();
                String traceId = Optional.ofNullable(metadataParams.get(TRACE_ID)).orElse(UNDEFINED);
                String spanId = Optional.ofNullable(metadataParams.get(SPAN_ID)).orElse(UNDEFINED);
                String sessionId = Optional.ofNullable(metadataParams.get(SESSION_ID)).orElse(UNDEFINED);
                Span span = TracingHelper.startNewSpanWithParent(traceId, spanId, sessionId, TRACER_NAME, PROCESS_NAME);
                try (Scope scope = span.makeCurrent()) {
                    handleFeedbackEvent(externalFeedbackEventPayload, metadataParams);
                } finally {
                    span.end();
                }

            }
        }
    }

    private void handleFeedbackEvent(ExternalFeedbackEvent externalFeedbackEventPayload,
                                     Map<String, String> metadataParams) {
        String communicationUsedForEventStr = metadataParams.getOrDefault(COMMUNICATION, "no communication found");
        String status = externalFeedbackEventPayload.getNotificationFeedback().getStatus();
        log.info("Engagement Suite: Received feedback event with communication {} for application id {} and trace id {} and status {}", communicationUsedForEventStr,
                metadataParams.getOrDefault(APPLICATION_ID, "no app id found"),
                metadataParams.getOrDefault(TRACE_ID, "no trace id id found"),
                status);

        SupportedCommunication communicationUsedForEvent;

        try {
            communicationUsedForEvent = SupportedCommunication.valueOf(communicationUsedForEventStr);
        } catch (IllegalArgumentException exception) {
            log.warn("Feedback event with unknown communication {} received", communicationUsedForEventStr);
            return;
        }

        FeedbackEvent feedbackEvent = FeedbackEventTransformer.transform(externalFeedbackEventPayload);

        FeedbackHandler feedbackHandler = feedbackHandlerFactory.getFeedbackHandler(communicationUsedForEvent);

        switch (status) {
            case FeedbackStatus.DELIVERED_TO_PROVIDER -> feedbackHandler.handleSuccess(feedbackEvent);
            case FeedbackStatus.NOT_ACCEPTED, FeedbackStatus.NOT_DELIVERED_TO_PROVIDER ->
                    feedbackHandler.handleFailure(feedbackEvent);
        }
    }
}

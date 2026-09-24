package com.ing.bankguarantees.remote.kafka.engagementsuite.consumer.feedbackhandler;

import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.FeedbackEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationEventParams.APPLICATION_ID;
import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationEventParams.RECEIVER_LR_NAME;


@Slf4j
public class SignCompletionEmailNotificationFeedbackHandler implements FeedbackHandler {

    @Override
    public void handleFailure(FeedbackEvent feedbackEvent) {
        Map<String, String> metadataParams = feedbackEvent.getHeader().getMetadata();
        log.error("""
                SignCompletionEmailNotificationFeedbackHandler [handleFailure] Engagement Suite: Email related to \
                Bank guarantee signature completion has not been sent to {} \
                with id {} : failure reason code {} : message {}
                """, metadataParams.get(RECEIVER_LR_NAME), metadataParams.get(APPLICATION_ID), feedbackEvent.getBody().getReason().getCode(), feedbackEvent.getBody().getReason().getDescription());


        log.error(C3LogMarker.marker, """
                SignCompletionEmailNotificationFeedbackHandler [handleFailure] Engagement Suite: Email related to \
                Bank guarantee signature completion has not been sent to {} \
                with id {} : failure reason code {} : message {}
                """, metadataParams.get(RECEIVER_LR_NAME), metadataParams.get(APPLICATION_ID),
                feedbackEvent.getBody().getReason().getCode(), feedbackEvent.getBody().getReason().getDescription());
    }

    @Override
    public void handleSuccess(FeedbackEvent feedbackEvent) {
        Map<String, String> metadataParams = feedbackEvent.getHeader().getMetadata();
        log.info("""
                SignCompletionEmailNotificationFeedbackHandler [handleSuccess] Engagement Suite: Email related to \
                 Bank guarantee signature completion  request has been successfully sent to {} \
                 with id {} """, metadataParams.get(RECEIVER_LR_NAME), metadataParams.get(APPLICATION_ID));
    }
}
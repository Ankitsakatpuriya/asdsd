package com.ing.bankguarantees.remote.kafka.engagementsuite.consumer.feedbackhandler;

import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.FeedbackEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationEventParams.APPLICATION_ID;


@Slf4j
public class StpResultsEmailNotificationFeedbackHandler implements FeedbackHandler {

    @Override
    public void handleFailure(FeedbackEvent feedbackEvent) {
        Map<String, String> metadataParams = feedbackEvent.getHeader().getMetadata();
        log.error("""
                StpResultsEmailNotificationFeedbackHandler [handleFailure] Engagement Suite: Email related to \
                Bank guarantee stp results email  has not been sent to clt \
                with id {} : failure reason code {} : message {}
                """, metadataParams.get(APPLICATION_ID), feedbackEvent.getBody().getReason().getCode(), feedbackEvent.getBody().getReason().getDescription());


        log.error(C3LogMarker.marker, """
                        StpResultsEmailNotificationFeedbackHandler [handleFailure] Engagement Suite: Email related to \
                        Bank guarantee stp results email  has not been sent to clt \
                        with id {} : failure reason code {} : message {}
                        """, metadataParams.get(APPLICATION_ID),
                feedbackEvent.getBody().getReason().getCode(), feedbackEvent.getBody().getReason().getDescription());
    }

    @Override
    public void handleSuccess(FeedbackEvent feedbackEvent) {
        Map<String, String> metadataParams = feedbackEvent.getHeader().getMetadata();
        log.info("""
                StpResultsEmailNotificationFeedbackHandler [handleSuccess] Engagement Suite: Email related to \
                Bank guarantee stp results   has been successfully sent to clt \
                with id {} """, metadataParams.get(APPLICATION_ID));
    }
}
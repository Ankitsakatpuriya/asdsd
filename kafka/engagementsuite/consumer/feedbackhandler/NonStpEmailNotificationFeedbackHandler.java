package com.ing.bankguarantees.remote.kafka.engagementsuite.consumer.feedbackhandler;

import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.FeedbackEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationEventParams.APPLICATION_ID;


@Slf4j
public class NonStpEmailNotificationFeedbackHandler implements FeedbackHandler {


    @Override
    public void handleFailure(FeedbackEvent feedbackEvent) {
        Map<String, String> metadataParams = feedbackEvent.getHeader().getMetadata();
        log.error("""
                Engagement Suite: Email related to \
                Bank guarantee non stp request has not been sent to clt mailbox \
                with id {} : failure reason code {} : message {}
                """, metadataParams.get(APPLICATION_ID), feedbackEvent.getBody().getReason().getCode(), feedbackEvent.getBody().getReason().getDescription());


        log.error(C3LogMarker.marker, """
                        Engagement Suite: Email related to \
                        Bank guarantee non stp request has not been sent to clt mailbox \
                        with id {} : failure reason code {} : message {}
                        """, metadataParams.get(APPLICATION_ID),
                feedbackEvent.getBody().getReason().getCode(), feedbackEvent.getBody().getReason().getDescription());

    }

    @Override
    public void handleSuccess(FeedbackEvent feedbackEvent) {
        Map<String, String> metadataParams = feedbackEvent.getHeader().getMetadata();
        log.info("""
                Engagement Suite: Email related to \
                Bank guarantee  non stp  request has been successfully sent to clt mailbox \
                with id {} """, metadataParams.get(APPLICATION_ID));
    }
}
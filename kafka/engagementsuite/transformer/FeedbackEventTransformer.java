package com.ing.bankguarantees.remote.kafka.engagementsuite.transformer;

import com.ing.bankguarantees.remote.kafka.engagementsuite.model.FeedbackEvent;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.FeedbackEventBody;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.FeedbackReason;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.Header;
import com.ing.tpa.esuite.notification.external.feedback.ExternalFeedbackEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationEventParams.COMMUNICATION;


@Slf4j
public class FeedbackEventTransformer {
    public static FeedbackEvent transform(ExternalFeedbackEvent externalFeedbackEvent) {
        log.info("FeedbackEventTransformer [transform] call");
        FeedbackEventBody feedbackEventBody = FeedbackEventBody.builder()
                .communication(externalFeedbackEvent.getNotificationMetadata().get(COMMUNICATION))
                .reason(FeedbackReason.builder()
                        .code(ObjectUtils.isEmpty(externalFeedbackEvent.getNotificationFeedback().getError())
                                ? null : externalFeedbackEvent.getNotificationFeedback().getError().getCode())
                        .description(ObjectUtils.isEmpty(externalFeedbackEvent.getNotificationFeedback().getError())
                                ? null : externalFeedbackEvent.getNotificationFeedback().getError().getMessage())
                        .build())
                .status(externalFeedbackEvent.getNotificationFeedback().getStatus())
                .build();

        return FeedbackEvent.builder()
                .header(Header.builder()
                        .metadata(externalFeedbackEvent.getNotificationMetadata())
                        .build())
                .body(feedbackEventBody)
                .build();
    }
}

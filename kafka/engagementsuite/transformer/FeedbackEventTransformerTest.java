package com.ing.bankguarantees.remote.kafka.engagementsuite.transformer;

import com.ing.bankguarantees.remote.kafka.engagementsuite.model.FeedbackEvent;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.tpa.esuite.notification.external.feedback.ExternalFeedbackEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@Slf4j
class FeedbackEventTransformerTest {

    private static final String FEEDBACK_EVENT_SUCCESSFUL_JSON = "BGA/BGF/ExternalFeedbackEventSuccessful.json";

    @Test
    @SneakyThrows
    void transformSuccessful() {

        ExternalFeedbackEvent externalFeedbackEvent = MockHelper.externalFeedbackEvent(FEEDBACK_EVENT_SUCCESSFUL_JSON);
        FeedbackEvent feedbackEvent = FeedbackEventTransformer.transform(externalFeedbackEvent);
        assertEquals("hello@company.com", feedbackEvent.getHeader().getMetadata().get("emailUsedForCustomer"));
        assertEquals("LOAN_APPLICATION_OFFER", feedbackEvent.getHeader().getMetadata().get("communication"));
        assertEquals("DELIVERED_TO_PROVIDER", feedbackEvent.getBody().getStatus());
    }
}
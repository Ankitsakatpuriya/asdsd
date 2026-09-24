package com.ing.bankguarantees.remote.kafka.engagementsuite.consumer;

import com.ing.bankguarantees.remote.kafka.engagementsuite.FeedbackEventSamples;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.SupportedCommunication;
import com.ing.bankguarantees.remote.kafka.engagementsuite.consumer.feedbackhandler.FeedbackHandlerFactory;
import com.ing.bankguarantees.remote.kafka.engagementsuite.consumer.feedbackhandler.NonStpEmailNotificationFeedbackHandler;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.FeedbackEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventFeedbackConsumerTest {

    private NotificationEventFeedbackConsumer underTest;

    @Mock
    private FeedbackHandlerFactory feedbackHandlerFactory;

    @Mock
    private NonStpEmailNotificationFeedbackHandler nonStpEmailNotificationFeedbackHandler;


    private static final String SUCCESSFULLY_DELIVERED = "DELIVERED_TO_PROVIDER";

    private static final String NOT_DELIVERED = "NOT_DELIVERED_TO_PROVIDER";

    @BeforeEach
    void setup() {
        underTest = new NotificationEventFeedbackConsumer(feedbackHandlerFactory);
    }

    @Test
    void nonStpNotificationTesting() {


        when(feedbackHandlerFactory.getFeedbackHandler(any(SupportedCommunication.class))).thenReturn(nonStpEmailNotificationFeedbackHandler);
        Mockito.doNothing().when(nonStpEmailNotificationFeedbackHandler).handleSuccess(any());
        underTest.listen(FeedbackEventSamples.getExternalFeedbackEventMessage(SupportedCommunication.NON_STP_EMAIL_NOTIFICATION.name(), SUCCESSFULLY_DELIVERED));
        verify(feedbackHandlerFactory, times(1)).getFeedbackHandler(SupportedCommunication.NON_STP_EMAIL_NOTIFICATION);
        verify(nonStpEmailNotificationFeedbackHandler, times(1)).handleSuccess(any(FeedbackEvent.class));

    }

    @Test
    void nonStpFeedbackFailedTesting() {

        when(feedbackHandlerFactory.getFeedbackHandler(any(SupportedCommunication.class))).thenReturn(nonStpEmailNotificationFeedbackHandler);
        Mockito.doNothing().when(nonStpEmailNotificationFeedbackHandler).handleFailure(any());

        underTest.listen(FeedbackEventSamples.getExternalFeedbackEventMessage(SupportedCommunication.NON_STP_EMAIL_NOTIFICATION.name(),
                NOT_DELIVERED,"not delivered","not delivered"));

        verify(feedbackHandlerFactory, times(1)).getFeedbackHandler(SupportedCommunication.NON_STP_EMAIL_NOTIFICATION);
        verify(nonStpEmailNotificationFeedbackHandler, times(1)).handleFailure(any(FeedbackEvent.class));

    }


}
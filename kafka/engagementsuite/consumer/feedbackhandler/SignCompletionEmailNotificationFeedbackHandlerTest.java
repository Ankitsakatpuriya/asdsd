package com.ing.bankguarantees.remote.kafka.engagementsuite.consumer.feedbackhandler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.ing.bankguarantees.remote.kafka.engagementsuite.FeedbackEventSamples;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.SupportedCommunication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignCompletionEmailNotificationFeedbackHandlerTest {

    @InjectMocks
    private SignCompletionEmailNotificationFeedbackHandler underTest;

    @Mock
    private Appender<ILoggingEvent> feedbackHandlerLogAppender;


    private static final String SUCCESSFULLY_DELIVERED = "DELIVERED";
    private static final String NOT_DELIVERED = "NOT DELIVERED";


    @BeforeEach
    void setup() {
        Logger signCompletionEmailNotificationFeedbackHandler = (Logger) LoggerFactory.getLogger(SignCompletionEmailNotificationFeedbackHandler.class);
        signCompletionEmailNotificationFeedbackHandler.addAppender(feedbackHandlerLogAppender);
    }

    @Test
    void testHandleSuccess() {
        underTest.handleSuccess(FeedbackEventSamples.getFeedbackEvent(SupportedCommunication.SIGN_COMPLETION_EMAIL_NOTIFICATION.name(), SUCCESSFULLY_DELIVERED));

        verify(feedbackHandlerLogAppender, atLeast(1)).doAppend(any());
        verify(feedbackHandlerLogAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("SignCompletionEmailNotificationFeedbackHandler [handleSuccess] Engagement Suite: Email related to  Bank guarantee signature completion  request has been successfully sent to")));
    }

    @Test
    void testHandleFailure() {
        underTest.handleFailure(FeedbackEventSamples.getFeedbackEvent(SupportedCommunication.CUSTOMER_EMAIL_NOTIFICATION.name(),
                NOT_DELIVERED));

        verify(feedbackHandlerLogAppender, atLeast(2)).doAppend(any());
        verify(feedbackHandlerLogAppender, times(2)).doAppend(argThat((arg) -> arg.getLevel() == Level.ERROR && arg.getFormattedMessage()
                .contains("SignCompletionEmailNotificationFeedbackHandler [handleFailure] Engagement Suite: Email related to Bank guarantee signature completion has not been sent to")));
    }


}

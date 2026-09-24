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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StpResultsEmailNotificationFeedbackHandlerTest {

    @InjectMocks
    private StpResultsEmailNotificationFeedbackHandler underTest;

    @Mock
    private Appender<ILoggingEvent> feedbackHandlerLogAppender;


    private static final String SUCCESSFULLY_DELIVERED = "DELIVERED";
    private static final String NOT_DELIVERED = "NOT DELIVERED";


    @BeforeEach
    void setup() {
        Logger stpResultsHandlerLogger = (Logger) LoggerFactory.getLogger(StpResultsEmailNotificationFeedbackHandler.class);
        stpResultsHandlerLogger.addAppender(feedbackHandlerLogAppender);
    }

    @Test
    void testHandleSuccess() {
        underTest.handleSuccess(FeedbackEventSamples.getFeedbackEvent(SupportedCommunication.STP_RESULTS_EMAIL_NOTIFICATION.name(), SUCCESSFULLY_DELIVERED));

        verify(feedbackHandlerLogAppender, atLeast(1)).doAppend(any());
        verify(feedbackHandlerLogAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("StpResultsEmailNotificationFeedbackHandler [handleSuccess] Engagement Suite: Email related to Bank guarantee stp results   has been successfully sent to clt with id")));
    }

    @Test
    void testHandleFailure() {
        underTest.handleFailure(FeedbackEventSamples.getFeedbackEvent(SupportedCommunication.STP_RESULTS_EMAIL_NOTIFICATION.name(),
                NOT_DELIVERED));

        verify(feedbackHandlerLogAppender, atLeast(2)).doAppend(any());
        verify(feedbackHandlerLogAppender, times(2)).doAppend(argThat((arg) -> arg.getLevel() == Level.ERROR
                && arg.getFormattedMessage()
                .contains("StpResultsEmailNotificationFeedbackHandler [handleFailure] Engagement Suite: Email related to Bank guarantee stp results email  has not been sent to clt with id")));
    }


}

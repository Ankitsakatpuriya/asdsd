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
class NonStpEmailNotificationFeedbackHandlerTest {

    @InjectMocks
    private NonStpEmailNotificationFeedbackHandler underTest;

    @Mock
    private Appender<ILoggingEvent> feedbackHandlerLogAppender;


    private static final String SUCCESSFULLY_DELIVERED = "DELIVERED";
    private static final String NOT_DELIVERED = "NOT DELIVERED";


    @BeforeEach
    void setup() {
        Logger nonStpEmailNotificationFeedbackHandlerLogger = (Logger) LoggerFactory.getLogger(NonStpEmailNotificationFeedbackHandler.class);
        nonStpEmailNotificationFeedbackHandlerLogger.addAppender(feedbackHandlerLogAppender);
    }

    @Test
    void testHandleSuccess() {
        underTest.handleSuccess(FeedbackEventSamples.getFeedbackEvent(SupportedCommunication.NON_STP_EMAIL_NOTIFICATION.name(), SUCCESSFULLY_DELIVERED));

        verify(feedbackHandlerLogAppender, atLeast(1)).doAppend(any());
        verify(feedbackHandlerLogAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                .contains("Engagement Suite: Email related to Bank guarantee  non stp  request has been successfully sent to clt mailbox with id applicationId")));
    }

    @Test
    void testHandleFailure() {
        underTest.handleFailure(FeedbackEventSamples.getFeedbackEvent(SupportedCommunication.NON_STP_EMAIL_NOTIFICATION.name(),
                NOT_DELIVERED));

        verify(feedbackHandlerLogAppender, atLeast(2)).doAppend(any());
        verify(feedbackHandlerLogAppender, times(2)).doAppend(argThat((arg) -> arg.getLevel() == Level.ERROR
                && arg.getFormattedMessage()
                .contains("Engagement Suite: Email related to Bank guarantee non stp request has not been sent to clt mailbox with " +
                        "id applicationId : failure reason code NOT DELIVERED : message NOT DELIVERED")));
    }

    @ParameterizedTest
    @MethodSource("supportedCommunications")
    void testFeedbackHandlerFactory(SupportedCommunication supportedCommunication) {
        FeedbackHandlerFactory feedbackHandlerFactory = new FeedbackHandlerFactory();
        FeedbackHandler feedbackHandler = feedbackHandlerFactory.getFeedbackHandler(supportedCommunication);
        assertThat(feedbackHandler).isNotNull();
        assertClassName(supportedCommunication, feedbackHandler.getClass().getName());

    }

    private void assertClassName(SupportedCommunication supportedCommunication, String className) {

        switch (supportedCommunication) {
            case NON_STP_EMAIL_NOTIFICATION:
                assertThat(className).isEqualTo(NonStpEmailNotificationFeedbackHandler.class.getName());
                break;
            case CUSTOMER_EMAIL_NOTIFICATION:
                assertThat(className).isEqualTo(CustomerEmailNotificationFeedbackHandler.class.getName());
                break;
            case BENEFICIARY_EMAIL_NOTIFICATION:
                assertThat(className).isEqualTo(BeneficiaryEmailNotificationFeedbackHandler.class.getName());
                break;
            case FULFILMENT_FAILURE_EMAIL_NOTIFICATION:
                assertThat(className).isEqualTo(FulfilmentFailureEmailNotificationFeedbackHandler.class.getName());
                break;
        }
    }

    private static Stream<SupportedCommunication> supportedCommunications() {
        return Stream.of(SupportedCommunication.values());
    }

}

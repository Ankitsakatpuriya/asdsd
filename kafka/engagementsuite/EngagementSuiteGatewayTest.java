package com.ing.bankguarantees.remote.kafka.engagementsuite;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.error.logger.ExceptionLogger;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.NotificationEvent;
import com.ing.bankguarantees.remote.kafka.engagementsuite.producer.callback.EmailNotificationCallback;
import com.ing.bankguarantees.remote.kafka.engagementsuite.transformer.NotificationEventTransformer;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.tpa.esuite.notificationapi.domain.EngagementSuiteNotificationEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class EngagementSuiteGatewayTest {

    private static final String ENGAGEMENT_SUIT_EVENT = "BGA/BGF/engagement_suit_payload.json";
    private static final String NOTIFICATION_EVENT = "BGA/BGF/notification_request.json";
    private static final String TOPIC = "EngagementSuite_OperationalNotificationEvent";


    @Mock
    private Producer<String, EngagementSuiteNotificationEvent> engagementSuitProducer;

    @Mock
    private NotificationEventTransformer notificationEventTransformer;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Mock
    private RecordMetadata metadata;

    @Mock
    private NotificationEvent notificationEvent;

    @Mock
    private NotificationPropertiesDetails notificationPropertiesDetails;

    @InjectMocks
    private EmailNotificationCallback notificationFutureCallback;

    private EngagementSuiteNotificationEvent engagementSuiteNotificationEvent;

    private EngagementSuiteGateway underTest;

    private static final ConditionFactory WAIT = await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofSeconds(1))
            .pollDelay(Duration.ofSeconds(1));


    @BeforeEach
    void setup() {
        underTest = new EngagementSuiteGateway(engagementSuitProducer, notificationEventTransformer, ExecutorConfig.workStealingPool());
        engagementSuiteNotificationEvent = MockHelper.creteEngagementSuiteNotificationEvent(ENGAGEMENT_SUIT_EVENT);
        notificationEvent = MockHelper.creteNotificationEvent(NOTIFICATION_EVENT);

        notificationFutureCallback = new EmailNotificationCallback(notificationEvent, notificationPropertiesDetails);
        lenient().when(notificationPropertiesDetails.getTopic()).thenReturn(TOPIC);

        Logger engagementSuiteGatewayLogger = (Logger) LoggerFactory.getLogger(EngagementSuiteGateway.class.getName());
        engagementSuiteGatewayLogger.setLevel(Level.DEBUG);
        engagementSuiteGatewayLogger.addAppender(mockAppender);

        Logger engagementSuiteGatewayTestLogger = (Logger) LoggerFactory.getLogger(EngagementSuiteGatewayTest.class.getName());
        engagementSuiteGatewayTestLogger.setLevel(Level.DEBUG);
        engagementSuiteGatewayTestLogger.addAppender(mockAppender);

        Logger nonStpFutureCallbacklogger = (Logger) LoggerFactory.getLogger(EmailNotificationCallback.class.getName());
        nonStpFutureCallbacklogger.setLevel(Level.DEBUG);
        nonStpFutureCallbacklogger.addAppender(mockAppender);

        Logger exceptionLogger = (Logger) LoggerFactory.getLogger(ExceptionLogger.class);
        exceptionLogger.setLevel(Level.DEBUG);
        exceptionLogger.addAppender(mockAppender);

    }

    @Test
    @SneakyThrows
    void sendPositive() {

        doAnswer(invocationOnMock -> {
            Callback callback = invocationOnMock.getArgument(1);
            callback.onCompletion(metadata, null);
            return null;
        }).when(engagementSuitProducer).send(any(), any());

        when(notificationEventTransformer.transform(any())).thenReturn(engagementSuiteNotificationEvent);

        underTest.send(notificationEvent, TOPIC, notificationFutureCallback);
        WAIT.untilAsserted(() -> {
            ArgumentCaptor<ProducerRecord> producerRecordArgumentCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
            verify(engagementSuitProducer).send(producerRecordArgumentCaptor.capture(), any());
            ProducerRecord<String, EngagementSuiteNotificationEvent> producerRecordValue = producerRecordArgumentCaptor.getValue();
            EngagementSuiteNotificationEvent expectedResult = producerRecordValue.value();
            assertThat(expectedResult).isEqualTo(engagementSuiteNotificationEvent);
            verify(mockAppender, atLeast(3)).doAppend(any());
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                    .contains("EngagementSuiteGateway [send]  Sending event to the topic EngagementSuite_OperationalNotificationEvent with communication NON_STP_EMAIL_NOTIFICATION for applicationId")));
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                    .contains("EngagementSuiteGateway [send]  Sending event to the topic EngagementSuite_OperationalNotificationEvent with communication NON_STP_EMAIL_NOTIFICATION, data")));
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                    .contains("EmailNotificationCallback [onSuccess] Engagement Suite: Kafka event successfully sent to topic EngagementSuite_OperationalNotificationEvent, communication NON_STP_EMAIL_NOTIFICATION for application id")));
        });
    }

    @Test
    @SneakyThrows
    void sendCallbackFailure() {

        doAnswer(invocationOnMock -> {
            Callback callback = invocationOnMock.getArgument(1);
            callback.onCompletion(null, new BgosException(ErrorCode.TECHNICAL_ERROR));
            return null;
        }).when(engagementSuitProducer).send(any(), any());

        when(notificationEventTransformer.transform(any())).thenReturn(engagementSuiteNotificationEvent);

        underTest.send(notificationEvent, TOPIC, notificationFutureCallback);
        WAIT.untilAsserted(() -> {
            ArgumentCaptor<ProducerRecord> producerRecordArgumentCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
            verify(engagementSuitProducer).send(producerRecordArgumentCaptor.capture(), any());
            ProducerRecord<String, EngagementSuiteNotificationEvent> producerRecordValue = producerRecordArgumentCaptor.getValue();
            EngagementSuiteNotificationEvent expectedResult = producerRecordValue.value();
            assertThat(expectedResult).isEqualTo(engagementSuiteNotificationEvent);
            verify(mockAppender, atLeast(4)).doAppend(any());
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                    .contains("EngagementSuiteGateway [send]  Sending event to the topic EngagementSuite_OperationalNotificationEvent with communication NON_STP_EMAIL_NOTIFICATION for applicationId")));
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                    .contains("EngagementSuiteGateway [send]  Sending event to the topic EngagementSuite_OperationalNotificationEvent with communication NON_STP_EMAIL_NOTIFICATION, data")));
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.ERROR && arg.getFormattedMessage()
                    .contains("EmailNotificationCallback [onFailure] Engagement Suite: Failed to send kafka event to the topic EngagementSuite_OperationalNotificationEvent with communication NON_STP_EMAIL_NOTIFICATION, and error message BGOS-00-001 for applicationId")));
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.ERROR && arg.getFormattedMessage()
                    .contains("STACK TRACE")));

        });
    }

    @Test
    @SneakyThrows
    void testKafkaException() {

        when(engagementSuitProducer.send(any(), any())).thenThrow(new BgosException(ErrorCode.TECHNICAL_ERROR));
        when(notificationEventTransformer.transform(any())).thenReturn(engagementSuiteNotificationEvent);

        underTest.send(notificationEvent, TOPIC, notificationFutureCallback);
        WAIT.untilAsserted(() -> {
            ArgumentCaptor<ProducerRecord> producerRecordArgumentCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
            verify(engagementSuitProducer).send(producerRecordArgumentCaptor.capture(), any());
            ProducerRecord<String, EngagementSuiteNotificationEvent> producerRecordValue = producerRecordArgumentCaptor.getValue();
            EngagementSuiteNotificationEvent expectedResult = producerRecordValue.value();
            assertThat(expectedResult).isEqualTo(engagementSuiteNotificationEvent);
            verify(mockAppender, atLeast(4)).doAppend(any());
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                    .contains("EngagementSuiteGateway [send]  Sending event to the topic EngagementSuite_OperationalNotificationEvent with communication NON_STP_EMAIL_NOTIFICATION for applicationId")));
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                    .contains("EngagementSuiteGateway [send]  Sending event to the topic EngagementSuite_OperationalNotificationEvent with communication NON_STP_EMAIL_NOTIFICATION, data")));
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.ERROR && arg.getFormattedMessage()
                    .contains("EmailNotificationCallback [onFailure] Engagement Suite: Failed to send kafka event to the topic EngagementSuite_OperationalNotificationEvent with communication NON_STP_EMAIL_NOTIFICATION, and error message BGOS-00-001 for applicationId")));
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.ERROR && arg.getFormattedMessage()
                    .contains("STACK TRACE")));

        });
    }

}
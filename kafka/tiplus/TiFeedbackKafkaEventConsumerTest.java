package com.ing.bankguarantees.remote.kafka.tiplus;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.error.logger.ExceptionLogger;
import com.ing.bankguarantees.service.finalization.BankGuaranteeFulfilmentService;
import com.ing.bodega.ReleaseNotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class TiFeedbackKafkaEventConsumerTest {

    private static final String TI_FEEDBACK_TOPIC = "P02727.bgi-release-notifications";
    private static final String MASTER_REFERENCE_ID = "BEBTFSBGR6000012";
    private static final String STATUS = "LIVE";

    private TiFeedbackKafkaEventConsumer underTest;

    @Mock
    private BankGuaranteeFulfilmentService bankGuaranteeFulfilmentService;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    public static ConditionFactory WAIT = await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofSeconds(1))
            .pollDelay(Duration.ofSeconds(1));

    @BeforeEach
    void setup() {
        underTest = new TiFeedbackKafkaEventConsumer(bankGuaranteeFulfilmentService);

        Logger logger = (Logger) LoggerFactory.getLogger(TiFeedbackKafkaEventConsumer.class.getName());
        logger.setLevel(Level.INFO);
        logger.addAppender(mockAppender);

        Logger exceptionLogger = (Logger) LoggerFactory.getLogger(ExceptionLogger.class);
        exceptionLogger.setLevel(Level.DEBUG);
        exceptionLogger.addAppender(mockAppender);
    }

    @Test
    void consumeHandleCallbackPositive() {

        ConsumerRecord<String, ReleaseNotificationEvent> consumerRecord = getTiCallbackEventMessage();
        when(bankGuaranteeFulfilmentService.processTiSuccessFulfilment(any())).thenReturn(CompletableFuture.completedFuture(null));
        underTest.consume(consumerRecord);
        WAIT.untilAsserted(() -> {
            ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
            verify(bankGuaranteeFulfilmentService, times(1)).processTiSuccessFulfilment(argumentCaptor.capture());
            String masterReferenceId = argumentCaptor.getValue();
            assertThat(masterReferenceId).isEqualTo(consumerRecord.value().getMasterReference());
            verify(mockAppender, atLeast(2)).doAppend(any());
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                    .contains("Receive Ti Plus feedback listener event " + consumerRecord.key() + ", value " + consumerRecord.value())));
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                    .contains("Ti plus callback processing finished for master reference " + consumerRecord.value().getMasterReference())));
        });
    }

    @Test
    void consumeHandleCallbackNegative() {

        ConsumerRecord<String, ReleaseNotificationEvent> consumerRecord = getTiCallbackEventMessage();
        when(bankGuaranteeFulfilmentService.processTiSuccessFulfilment(any())).thenReturn(CompletableFuture.failedFuture(new BgosException(ErrorCode.TECHNICAL_ERROR)));
        underTest.consume(consumerRecord);
        WAIT.untilAsserted(() -> {
            ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
            verify(bankGuaranteeFulfilmentService, times(1)).processTiSuccessFulfilment(argumentCaptor.capture());
            String masterReferenceId = argumentCaptor.getValue();
            assertThat(masterReferenceId).isEqualTo(consumerRecord.value().getMasterReference());
            verify(mockAppender, atLeast(4)).doAppend(any());
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                    .contains("Receive Ti Plus feedback listener event " + consumerRecord.key() + ", value " + consumerRecord.value())));
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.ERROR && arg.getFormattedMessage()
                    .contains("Error occur while processing Ti Plus callback")));
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.ERROR && arg.getFormattedMessage()
                    .contains("STACK TRACE")));
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                    .contains("Ti plus callback processing finished for master reference " + consumerRecord.value().getMasterReference())));
        });
    }

    private static ConsumerRecord<String, ReleaseNotificationEvent> getTiCallbackEventMessage() {
        ReleaseNotificationEvent releaseNotificationEvent = ReleaseNotificationEvent.newBuilder()
                .setMasterReference(MASTER_REFERENCE_ID)
                .setStatus(STATUS)
                .build();
        return new ConsumerRecord<>(TI_FEEDBACK_TOPIC, 1, 10L, null, releaseNotificationEvent);
    }
}
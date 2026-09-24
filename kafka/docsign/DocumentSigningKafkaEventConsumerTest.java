package com.ing.bankguarantees.remote.kafka.docsign;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.error.logger.ExceptionLogger;
import com.ing.bankguarantees.service.documentsigning.DarCallbackHandlerService;
import com.ing.bankguarantees.service.finalization.BankGuaranteeFinalizationService;
import com.ing.docsign.CallbackEvent;
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
class DocumentSigningKafkaEventConsumerTest {

    private DocumentSigningKafkaEventConsumer underTest;

    @Mock
    private DarCallbackHandlerService darCallbackHandlerService;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    public static ConditionFactory WAIT = await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofSeconds(1))
            .pollDelay(Duration.ofSeconds(1));

    @BeforeEach
    void setup() {
        underTest = new DocumentSigningKafkaEventConsumer(darCallbackHandlerService);

        Logger logger = (Logger) LoggerFactory.getLogger(DocumentSigningKafkaEventConsumer.class.getName());
        logger.setLevel(Level.INFO);
        logger.addAppender(mockAppender);

        Logger exceptionLogger = (Logger) LoggerFactory.getLogger(ExceptionLogger.class);
        exceptionLogger.setLevel(Level.DEBUG);
        exceptionLogger.addAppender(mockAppender);
    }

    @Test
    void consumeHandleCallbackPositive() {

        ConsumerRecord<String, CallbackEvent> consumerRecord = getDocumentSigningEventMessage("DONE");
        when(darCallbackHandlerService.handleAfterSignCallback(any())).thenReturn(CompletableFuture.completedFuture(null));
        underTest.consume(consumerRecord);
        WAIT.untilAsserted(() -> {
            ArgumentCaptor<CallbackEvent> argumentCaptor = ArgumentCaptor.forClass(CallbackEvent.class);
            verify(darCallbackHandlerService, times(1)).handleAfterSignCallback(argumentCaptor.capture());
            CallbackEvent callbackEvent = argumentCaptor.getValue();
            assertThat(callbackEvent).isEqualTo(consumerRecord.value());
            verify(mockAppender, atLeast(2)).doAppend(any());
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                    .contains("Receive  document sign listener event " + consumerRecord.key() + ", value " + consumerRecord.value())));
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                    .contains("Finalizing bank guarantee after customer sign is completed for request id " + callbackEvent.getAgreementId())));
        });
    }

    @Test
    void consumeHandleCallbackNegative() {

        ConsumerRecord<String, CallbackEvent> consumerRecord = getDocumentSigningEventMessage("DONE");
        when(darCallbackHandlerService.handleAfterSignCallback(any())).thenReturn(CompletableFuture.failedFuture(new BgosException(ErrorCode.TECHNICAL_ERROR)));
        underTest.consume(consumerRecord);
        WAIT.untilAsserted(() -> {
            ArgumentCaptor<CallbackEvent> argumentCaptor = ArgumentCaptor.forClass(CallbackEvent.class);
            verify(darCallbackHandlerService, times(1)).handleAfterSignCallback(argumentCaptor.capture());
            CallbackEvent callbackEvent = argumentCaptor.getValue();
            assertThat(callbackEvent).isEqualTo(consumerRecord.value());
            verify(mockAppender, atLeast(4)).doAppend(any());
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                    .contains("Receive  document sign listener event " + consumerRecord.key() + ", value " + consumerRecord.value())));
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.ERROR && arg.getFormattedMessage()
                    .contains("Error while starting fulfilment process")));
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.ERROR && arg.getFormattedMessage()
                    .contains("STACK TRACE")));
            verify(mockAppender, times(1)).doAppend(argThat((arg) -> arg.getLevel() == Level.INFO && arg.getFormattedMessage()
                    .contains("Finalizing bank guarantee after customer sign is completed for request id " + callbackEvent.getAgreementId())));
        });
    }

    private static ConsumerRecord<String, CallbackEvent> getDocumentSigningEventMessage(String status) {
        CallbackEvent event = CallbackEvent.newBuilder()
                .setStatus(status)
                .setAgreementId("123")
                .setDarUuid("456")
                .setCreationDateTime("2025-03-31T12:31:39.633")
                .setStatusChangeDateTime("2025-03-31T14:27:12.156")
                .build();
        return new ConsumerRecord<>("bgos-docsign-callback", 1, 10L, null, event);
    }
}
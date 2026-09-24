package com.ing.bankguarantees.remote.kafka.datalakeevent;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.ing.bankguarantees.avro.BankGuaranteesDataLakeEvent;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.error.logger.ExceptionLogger;
import com.ing.bankguarantees.remote.kafka.datalakeevent.producer.DataLakeEventGateway;
import com.ing.bankguarantees.util.MockHelper;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataLakeEventGatewayTest {

    private static final String BG_REQUEST_DATA_LAKE_FILE = "BGA/DLD/bg_request_data_lake.json";

    @Mock
    private Producer<String, BankGuaranteesDataLakeEvent> dataLakeProducer;

    @Mock
    private DataLakeKafkaProperties dataLakeEventProperties;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Mock
    private RecordMetadata metadata;

    @InjectMocks
    private DataLakeEventGateway dataLakeEventGateway;

    private BankGuaranteesDataLakeEvent notificationEvent;


    public static ConditionFactory WAIT = await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofSeconds(1))
            .pollDelay(Duration.ofSeconds(1));

    @BeforeEach
    void setup() {

        when(dataLakeEventProperties.getTopic()).thenReturn("ing_Aurora_bankguarantee_application");

        Logger dataLakeEventGatewayLogger =
                (Logger) LoggerFactory.getLogger(DataLakeEventGateway.class);
        dataLakeEventGatewayLogger.setLevel(Level.DEBUG);
        dataLakeEventGatewayLogger.addAppender(mockAppender);
        Logger dataLakeEventGatewayTestLogger =
                (Logger) LoggerFactory.getLogger(DataLakeEventGatewayTest.class);
        dataLakeEventGatewayTestLogger.setLevel(Level.DEBUG);
        dataLakeEventGatewayTestLogger.addAppender(mockAppender);
        Logger exceptionLogger =
                (Logger) LoggerFactory.getLogger(ExceptionLogger.class);
        exceptionLogger.setLevel(Level.DEBUG);
        exceptionLogger.addAppender(mockAppender);


        notificationEvent = MockHelper.createBankGuaranteeRequestDataLake(BG_REQUEST_DATA_LAKE_FILE);

    }


    @Test
    void sendPositive() {
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onCompletion(metadata, null);
            return null;
        }).when(dataLakeProducer).send(any(), any());

        CompletableFuture<Boolean> future = dataLakeEventGateway.send(notificationEvent);

        WAIT.untilAsserted(() -> {
            assertTrue(future.isDone());
            assertFalse(future.isCompletedExceptionally());
            assertTrue(future.join());

            verify(dataLakeProducer).send(any(), any());

            verify(mockAppender, times(1)).doAppend(argThat(arg ->
                    arg.getLevel() == Level.INFO &&
                            arg.getFormattedMessage().contains(
                                    "DataLakeEventGateway [send] Sending event to the topic ing_Aurora_bankguarantee_application with eventName DRAFT"
                            )));

            verify(mockAppender, times(1)).doAppend(argThat(arg ->
                    arg.getLevel() == Level.INFO &&
                            arg.getFormattedMessage().contains(
                                    "Kafka event successfully sent"
                            )));
        });
    }

    @Test
    void testFailureCallback() {
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onCompletion(null, new BgosException(ErrorCode.TECHNICAL_ERROR));
            return null;
        }).when(dataLakeProducer).send(any(), any());

        CompletableFuture<Boolean> future = dataLakeEventGateway.send(notificationEvent);

        WAIT.untilAsserted(() -> {
            assertTrue(future.isDone());
            assertTrue(future.isCompletedExceptionally());

            assertThrows(CompletionException.class, future::join);

            verify(dataLakeProducer).send(any(), any());

            verify(mockAppender, times(1)).doAppend(argThat(arg ->
                    arg.getLevel() == Level.ERROR &&
                            arg.getFormattedMessage().contains(
                                    "Kafka Exception"
                            )));
        });
    }


    @Test
    void testKafkaException() {
        when(dataLakeProducer.send(any(), any()))
                .thenThrow(new BgosException(ErrorCode.TECHNICAL_ERROR));

        CompletableFuture<Boolean> future = dataLakeEventGateway.send(notificationEvent);

        WAIT.untilAsserted(() -> {
            assertTrue(future.isDone());
            assertTrue(future.isCompletedExceptionally());

            CompletionException ce = assertThrows(CompletionException.class, future::join);
            assertTrue(ce.getCause() instanceof BgosException);

            ArgumentCaptor<ILoggingEvent> captor = ArgumentCaptor.forClass(ILoggingEvent.class);
            verify(mockAppender, atLeastOnce()).doAppend(captor.capture());

            List<ILoggingEvent> events = captor.getAllValues();

            assertTrue(events.stream().anyMatch(e ->
                    e.getLevel() == Level.ERROR &&
                            e.getFormattedMessage().contains("Kafka Exception")
            ));

            assertTrue(events.stream().anyMatch(e ->
                    e.getLevel() == Level.ERROR &&
                            e.getThrowableProxy() != null
            ));
        });
    }
}

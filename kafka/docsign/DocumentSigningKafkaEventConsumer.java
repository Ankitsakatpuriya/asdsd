package com.ing.bankguarantees.remote.kafka.docsign;

import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.logger.ExceptionLogger;
import com.ing.bankguarantees.service.documentsigning.DarCallbackHandlerService;
import com.ing.bankguarantees.utils.TracingHelper;
import com.ing.docsign.CallbackEvent;
import com.ing.docsign.callback.DetailedCallbackEvent;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentSigningKafkaEventConsumer {

    private static final String DOCUMENT_SIGN_TOPIC = "bgos-docsign-callback";
    private static final String DOCUMENT_SIGN_INTERMEDIATE_TOPIC = "P31464.bgos-docsign-intermediate-callback";
    private static final String CALLBACK_FACTORY = "documentSigningListenerContainerFactory";
    private static final String INTERMEDIATE_FACTORY = "docSignIntermediateCallbackContainerFactory";
    private static final String TRACER_NAME = "signature_callback_tracer";
    private static final String PROCESS_NAME = "process_signature_callback";

    private final DarCallbackHandlerService darCallbackHandlerService;

    @KafkaListener(topics = DOCUMENT_SIGN_TOPIC, containerFactory = CALLBACK_FACTORY)
    public void consume(ConsumerRecord<String, CallbackEvent> documentSignEventRecord) {
        log.info("Receive  document sign listener event {}, value {}", documentSignEventRecord.key(), documentSignEventRecord.value());
        Span span = TracingHelper.startFreshSpan(TRACER_NAME, PROCESS_NAME);
        try (Scope scope = span.makeCurrent()) {
            CallbackEvent callbackEvent = documentSignEventRecord.value();
            darCallbackHandlerService.handleAfterSignCallback(callbackEvent)
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            ExceptionLogger.error(exception, C3LogMarker.marker, "Error while starting fulfilment process");
                        }
                        log.info("Finalizing bank guarantee after customer sign is completed for request id {}", callbackEvent.getAgreementId());

                    });
        } finally {
            span.end();
        }

    }

    @KafkaListener(topics = DOCUMENT_SIGN_INTERMEDIATE_TOPIC, containerFactory = INTERMEDIATE_FACTORY)
    public void consumeIntermediate(DetailedCallbackEvent intermediateCallbackEvent) {
        log.info("Receive doc sign intermediate callback value {}", intermediateCallbackEvent);
        Span span = TracingHelper.startFreshSpan(TRACER_NAME, PROCESS_NAME);
        try (Scope scope = span.makeCurrent()) {
            darCallbackHandlerService.handleIntermediateCallback(intermediateCallbackEvent)
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            ExceptionLogger.error(exception, C3LogMarker.marker, "Error while performing partial signed operation");
                        }
                        log.info("Partially signed operation has been successfully completed for request id {}", intermediateCallbackEvent.getDarInfo().getAgreementId());

                    });
        } finally {
            span.end();
        }

    }
}


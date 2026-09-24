package com.ing.bankguarantees.remote.kafka.tiplus;

import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.logger.ExceptionLogger;
import com.ing.bankguarantees.service.finalization.BankGuaranteeFulfilmentService;
import com.ing.bankguarantees.utils.TracingHelper;
import com.ing.bodega.ReleaseNotificationEvent;
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
public class TiFeedbackKafkaEventConsumer {

    private static final String TI_FEEDBACK_TOPIC = "P02727.bgi-release-notifications";
    private static final String TRACER_NAME = "ti_plus_callback_tracer";
    private static final String PROCESS_NAME = "process_ti_plus_callback";

    private final BankGuaranteeFulfilmentService bankGuaranteeFulfilmentService;

    @KafkaListener(topics = TI_FEEDBACK_TOPIC, containerFactory = "tiPlusFeedbackListenerContainerFactory")
    public void consume(ConsumerRecord<String, ReleaseNotificationEvent> tiPlusEvent) {
        log.info("Receive Ti Plus feedback listener event {}, value {}", tiPlusEvent.key(), tiPlusEvent.value());
        Span span = TracingHelper.startFreshSpan(TRACER_NAME, PROCESS_NAME);
        try (Scope scope = span.makeCurrent()) {
            ReleaseNotificationEvent releaseNotificationEvent = tiPlusEvent.value();
            bankGuaranteeFulfilmentService.processTiSuccessFulfilment(releaseNotificationEvent.getMasterReference())
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            ExceptionLogger.error(exception, C3LogMarker.marker, "Error occur while processing Ti Plus callback");
                        }
                        log.info("Ti plus callback processing finished for master reference {}", releaseNotificationEvent.getMasterReference());
                    });

        } finally {
            span.end();
        }

    }
}


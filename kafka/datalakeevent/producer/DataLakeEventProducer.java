package com.ing.bankguarantees.remote.kafka.datalakeevent.producer;

import com.ing.bankguarantees.avro.BankGuaranteeEventBody;
import com.ing.bankguarantees.avro.BankGuaranteesDataLakeEvent;
import com.ing.bankguarantees.avro.Header;
import com.ing.bankguarantees.remote.kafka.datalakeevent.model.DataLakeEventDto;
import lombok.RequiredArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.ing.bankguarantees.remote.kafka.datalakeevent.utils.DataLakeUtils.Host;

@RequiredArgsConstructor
public abstract class DataLakeEventProducer {

    private static final String TIME_ZONE = "Europe/Brussels";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private final DataLakeEventGateway dataLakeEventGateway;

    abstract BankGuaranteeEventBody getEventSpecificData(DataLakeEventDto dataLakeEventDto);

    public CompletableFuture<Boolean> notify(DataLakeEventDto dataLakeEventDto) {
        return dataLakeEventGateway.send(getBGDataLakeEvent(dataLakeEventDto));
    }

    public BankGuaranteesDataLakeEvent getBGDataLakeEvent(DataLakeEventDto dataLakeEventDto) {
        return BankGuaranteesDataLakeEvent.newBuilder()
                .setHeader(getDataLakeHeader(dataLakeEventDto))
                .setBody(getDataLakeBody(dataLakeEventDto))
                .build();
    }

    private Header getDataLakeHeader(DataLakeEventDto dataLakeEventDto) {
        return Header.newBuilder()
                .setSessionId(dataLakeEventDto.getSessionId())
                .setEventId(UUID.randomUUID().toString())
                .setSource(Host.get())
                .setCreated(ZonedDateTime.now(ZoneId.of(TIME_ZONE)).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
                .setEventName(dataLakeEventDto.getEventName())
                .setTraceId(dataLakeEventDto.getTraceId())
                .setConfidentiality(dataLakeEventDto.getConfidentiality())
                .setVersion(dataLakeEventDto.getVersion())
                .build();
    }

    private BankGuaranteeEventBody getDataLakeBody(DataLakeEventDto dataLakeEventDto) {
        return getEventSpecificData(dataLakeEventDto);
    }

}

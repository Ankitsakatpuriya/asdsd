package com.ing.bankguarantees.remote.kafka.datalakeevent.model;

import com.ing.bankguarantees.avro.EventName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DataLakeEventDto {

    private String sessionId;
    private boolean filter;
    private EventName eventName;
    private String traceId;
    private int confidentiality;
    private String version;

}



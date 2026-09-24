package com.ing.bankguarantees.remote.kafka.engagementsuite.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Header {

    /**
     * GUID (UUID) of the event in order to make it globally identifiable
     */
    private String id;

    /**
     * Identifier which is set commonly to a number of events belonging to the same context (tree of execution)
     */
    private String traceId;

    /**
     * Event creation timestamp in milliseconds in UTC
     */
    private long created;

    private Source source;

    /**
     * Map with general purpose information related with the event.
     */
    private Map<String, String> metadata;

}











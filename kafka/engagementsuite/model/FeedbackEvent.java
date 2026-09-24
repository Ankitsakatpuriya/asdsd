package com.ing.bankguarantees.remote.kafka.engagementsuite.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackEvent {
    private Header header;
    private FeedbackEventBody body;

}











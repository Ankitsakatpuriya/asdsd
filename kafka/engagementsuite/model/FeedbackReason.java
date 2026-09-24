
package com.ing.bankguarantees.remote.kafka.engagementsuite.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackReason {

    /**
     * Identifier for the Feedback reason.
     */
    private String code;
    /**
     * Verbose explanation of the Feedback reason.
     */
    private String description;

}











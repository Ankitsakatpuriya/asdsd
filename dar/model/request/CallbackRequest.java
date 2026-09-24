package com.ing.bankguarantees.remote.rest.dar.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CallbackRequest {

    private String topic;

    private Set<TriggerStatusEnum> triggerStatus;

    public enum TriggerStatusEnum {
        DONE,

        CANCELLED,

        REVOKED,

        EXPIRED

    }

}


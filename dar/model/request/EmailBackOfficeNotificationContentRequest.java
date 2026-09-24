package com.ing.bankguarantees.remote.rest.dar.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailBackOfficeNotificationContentRequest {

    private String communicationLanguage;
    private Set<TriggerStatusEnum> triggerStatus;
    private String businessMessage;
    private String emailAddress;

    public enum TriggerStatusEnum {
        BLUE_INK,

        DRAFT,

        REMINDER,

        CANCELLED,

        REVOKED,

        EXPIRED,

        PARTIALLY_DONE,

        DONE
    }
}


package com.ing.bankguarantees.remote.rest.dar.model.request;

import java.util.Set;

public enum TriggerStatusV3 {

    DRAFT,

    DONE,

    CANCELLED,

    REVOKED,

    EXPIRED,

    PARTIALLY_DONE,

    REMINDER;


    public static Set<TriggerStatusV3> getCallbackStatus() {
        return Set.of(DONE, CANCELLED, REVOKED, EXPIRED, PARTIALLY_DONE);
    }
}

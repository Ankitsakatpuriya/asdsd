
package com.ing.bankguarantees.remote.kafka.engagementsuite.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackEventBody {
    /**
     * Identifies the message. Matches with header.id of notification event
     */
    private String messageId;
    /**
     * Identifies the customer. Matches with body.profileId of notification event
     */
    private String profileId;
    /**
     * Identifies the channel of the notification. Matches with body.channel of notification event
     */
    private String channel;
    /**
     * An identifier about what language should be used to notify the customer
     */
    private String locale;
    /**
     * Identifies the address of the notification. Matches with body.recipientAddress of notification event
     */
    private String recipientAddress;
    /**
     * Identifies the template reference of the notification. Matches with body.templateReference of notification event
     */
    private String templateReference;
    /**
     * Identifies when the notification event was transmitted to the Eventbus in UTC milliseconds (NotificationEvent.header
     * .created)
     */
    private long notificationCreated;
    /**
     * Identifies the communication. Matches with body.communication of notification event
     */
    private String communication;
    /**
     * Identifies the status of the notification event.
     */
    private String status;
    /**
     * Identifies the reason of the tracking event. It a class that has two fields: code and description
     */
    private FeedbackReason reason;

}











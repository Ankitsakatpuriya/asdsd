package com.ing.bankguarantees.remote.kafka.engagementsuite.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventBody {

    /**
     * A unique identifier for an ING customer
     */
    private String profileId;

    /**
     * A FQND name of the channel which should be used for sending out the notification
     */
    private String channel;

    /**
     * Map with information that will be interpreted by the channel
     */
    private Map<String, String> channelParams;


    /**
     * Channel Specific cc addresses
     */
    private List<String> ccAddresses;

    /**
     * An identifier about what locale should be used to notificate to the customer
     */
    private String locale;

    /**
     * Channel specific sender address information.
     */
    private String senderAddress;

    /**
     * Channel specific address information which is needed by the channel
     */
    private String recipientAddress;

    /**
     * A FQND reference to the template which should be used for rendering the message
     */
    private String communication;

    /**
     * A FQND reference to the template which should be used for rendering the message
     */
    private String templateReference;

    /**
     * Parameters which should be replaced in the template referenced in body.templateReference
     */
    private Map<String, String> templateParams;

    /**
     * specifies the date and time in ISO 8601 format when the message must be physically sent
     */
    private Long schedule;

}











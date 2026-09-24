package com.ing.bankguarantees.remote.kafka.engagementsuite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPropertiesDetails {
    private String senderAddress;
    private String recipientAddress;
    private List<String> ccAddresses;
    private String channel;
    private String templateReference;
    private String attachmentTemplateReference;
    private String attachmentFileName;
    private String attachmentContentType;
    private String topic;
    private String aedEndpoint;
    private String attachmentUrl;
}

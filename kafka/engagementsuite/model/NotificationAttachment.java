
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
public class NotificationAttachment {

    private String fileName;
    private String aedEndpoint;
    private String templateName;
    private String attachmentUrl;
    private String templateLocale;
    private String fileContentType;
    private Map<String, String> templateParams;
    private AttachmentProcessorType processorType;
}











package com.ing.bankguarantees.remote.kafka.engagementsuite;

import com.ing.bankguarantees.remote.utils.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@ConfigurationProperties("notification")
@PropertySource(value = "classpath:notification.yml", factory = YamlPropertySourceFactory.class)
public class NotificationProperties {

    private NotificationPropertiesDetails nonStpEmailNotificationProperties;
    private NotificationPropertiesDetails customerEmailNotificationProperties;
    private NotificationPropertiesDetails stpResultsEmailNotificationProperties;
    private NotificationPropertiesDetails beneficiaryEmailNotificationProperties;
    private NotificationPropertiesDetails signCompletionEmailNotificationProperties;
    private NotificationPropertiesDetails fulfilmentFailureEmailNotificationProperties;
    private NotificationPropertiesDetails signatureReminderEmailNotificationProperties;
    private NotificationPropertiesDetails requesterPartiallySignedEmailNotificationProperties;

}

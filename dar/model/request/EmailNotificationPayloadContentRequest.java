package com.ing.bankguarantees.remote.rest.dar.model.request;



import com.fasterxml.jackson.annotation.JsonFormat;
import com.ing.bankguarantees.utils.ConstantUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationPayloadContentRequest {

    private String communicationLanguage;
    private Set<TriggerStatusEnum> triggerStatus;
    private String actorId;
    private String businessMessage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConstantUtils.DATE_TIME_FORMAT)
    @JsonDeserialize(contentUsing = LocalDateTimeDeserializer.class)
    @JsonSerialize(contentUsing = LocalDateTimeSerializer.class)
    private List<LocalDateTime> notificationDates;

    private String emailAddress;

    public enum TriggerStatusEnum {
        BLUE_INK,

        DRAFT,

        REMINDER,

        CANCELLED,

        REVOKED,

        EXPIRED,

        DONE
    }
}


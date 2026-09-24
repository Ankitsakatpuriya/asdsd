package com.ing.bankguarantees.remote.rest.dar.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Builder
@Validated
@NoArgsConstructor
@AllArgsConstructor
public class CustomerNotificationChannelRequest {
    private List<EmailNotificationPayloadContentRequest> email;

}


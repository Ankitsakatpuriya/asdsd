package com.ing.bankguarantees.remote.kafka.engagementsuite.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Valid
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    @Valid
    @NotNull
    private Header header;

    @Valid
    @NotNull
    private NotificationEventBody body;

    @Valid
    @NotNull
    private List<NotificationAttachment> attachments;

    private SupportedCommunication type;

}











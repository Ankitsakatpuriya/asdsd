package com.ing.bankguarantees.remote.rest.connectdot.model.request;

import com.ing.bankguarantees.remote.rest.connectdot.model.payload.BaseDocumentPayload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@NotNull
@Builder
public class ConnectDotRequest<T extends BaseDocumentPayload> {

    private Identifiers identifiers;

    private MetaData metaData;

    private Recipient recipient;

    private List<Channel> channels;

    private T payload;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Identifiers {
        @NotBlank
        private UUID requestId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MetaData {
        private String costCenter;
        private String entityCode;
        private String requestDate;
        private String initiatingCI;
        private String communicationDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Recipient {
        private String type;
        private String partyType;
        private String preferredLanguage;
        private List<DigitalAddress> digitalAddresses;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Channel {
        private Set<Destination> destinations;
        private String importance;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Destination {
        private String templateName;
        private String output;
        private String type;
        private String documentId;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DigitalAddress {
        private String type;
        private String fullDigitalAddress;
    }


}

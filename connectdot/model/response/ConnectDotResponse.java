package com.ing.bankguarantees.remote.rest.connectdot.model.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response Payload from the response of Connect Dot API
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@NotNull
public class ConnectDotResponse {

    @NotBlank
    private String requestId;

    @NotEmpty
    @Valid
    private List<Result> results;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Result {

        private String type;
        private String distributionId;
        private Recipient recipient;
        private boolean success;
        private String template;
        private ExceptionDto exception;
        private List<Content> contents;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Recipient {

        private String type;
        private String id;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class Content {
        private String contentId;
        private String contentType;
        private String contentTransferEncoding;
        private String data;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class ExceptionDto {
        private String code;
        private String message;
        private String severity;
    }

}

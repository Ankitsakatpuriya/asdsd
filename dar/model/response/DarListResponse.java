package com.ing.bankguarantees.remote.rest.dar.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DarListResponse {
    @NotNull
    private List<DarResponse> data;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DarResponse {
        private String darUuid;
        private String transactionStatus;
        private String transactionId;
    }
}

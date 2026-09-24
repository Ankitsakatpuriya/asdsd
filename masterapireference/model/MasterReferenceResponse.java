package com.ing.bankguarantees.remote.rest.masterapireference.model;

import lombok.Builder;

@Builder
public record MasterReferenceResponse(String masterReferenceId, String timestamp) {
}

package com.ing.bankguarantees.remote.rest.masterapireference.model;

import lombok.Builder;

@Builder
public record MasterReferenceRequest(String branchId, String productId) {
}

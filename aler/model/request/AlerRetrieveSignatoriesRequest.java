package com.ing.bankguarantees.remote.rest.aler.model.request;

import lombok.Builder;

@Builder
public record AlerRetrieveSignatoriesRequest(String organisationIdentifier, boolean onlyActiveSignatories) {

}

package com.ing.bankguarantees.remote.rest.intake.model.request;

import lombok.Builder;

@Builder
public record IntakeApiRequest(PegaParamRequest parameters, ApplicationRequest applicationRequest) {


}

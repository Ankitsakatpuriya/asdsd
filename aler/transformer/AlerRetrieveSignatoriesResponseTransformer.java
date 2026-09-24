package com.ing.bankguarantees.remote.rest.aler.transformer;

import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerRetrieveSignatoriesResponse;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerRetrieveSignatoriesResponse.ErrorResponse;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerRetrieveSignatoriesResponse.SignatoryResponse;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerSignatories;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerSignatories.AlerErrorDetails;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerSignatories.Signatory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.nonNull;

@Slf4j
@Component
public class AlerRetrieveSignatoriesResponseTransformer implements Transformer<AlerRetrieveSignatoriesResponse, AlerSignatories> {

    @Override
    public AlerSignatories transform(AlerRetrieveSignatoriesResponse response) {
        log.info("receive response from Aler retrieve signatories ");
        return AlerSignatories.builder()
                .transactionId(response.transactionId())
                .transactionStatus(response.transactionStatus())
                .errorDetails(getErrorDetails(response.errorResponseDetails()))
                .signatories(getSignatories(response.signatoryResponse()))
                .build();
    }

    private List<Signatory> getSignatories(SignatoryResponse signatoryResponse) {
        return nonNull(signatoryResponse) ?
                signatoryResponse.signatories().stream()
                        .filter(signatory -> nonNull(signatory.signingPower()))
                        .map(signatory -> Signatory.builder()
                                .signatoryUUID(signatory.individualIdentifier())
                                .isActive(signatory.isActive())
                                .signingPower(signatory.signingPower().signingPowerValue())
                                .build())
                        .toList() : Collections.emptyList();

    }

    private AlerErrorDetails getErrorDetails(ErrorResponse errorResponse) {
        return nonNull(errorResponse) ? AlerErrorDetails.builder()
                .code(errorResponse.code())
                .message(errorResponse.message())
                .build()
                : null;
    }
}

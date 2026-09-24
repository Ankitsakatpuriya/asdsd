package com.ing.bankguarantees.remote.rest.creditdecision.model.request;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;


/**
 * @param organisationId CSI
 * @param individualId   CSI
 */
@Builder
public record CreditDecisionInput(BigInteger requestId, AccessToken accessToken,
                                  PamRequestIdentifier organisationId,
                                  PamRequestIdentifier individualId,
                                  BigDecimal amount) {

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PamRequestIdentifier {
        private String type;
        private String value;
    }

}

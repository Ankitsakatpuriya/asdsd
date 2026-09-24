package com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.transformer;


import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.model.response.ExternalIdentifierListResponse;
import com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.model.response.ExternalIdentifierResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Optional;
import static com.ing.bankguarantees.utils.JsonUtils.getJsonFromObject;


/**
 * Response transformer
 */
@Component("extIdentRespTransformer")
@RequiredArgsConstructor
@Slf4j
public class ExtIdentRespTransformer implements Transformer<ExternalIdentifierListResponse, Optional<String>> {

    private static final String KBO_BE = "KBO_BE";

    @Override
    public Optional<String> transform(ExternalIdentifierListResponse extIdentListResponse) {

        log.info(C3LogMarker.marker, "Receive response from Involved Party API  endpoint : {}", getJsonFromObject(extIdentListResponse));

        for (ExternalIdentifierResponse extIdentifier : extIdentListResponse.getInvolvedPartyExternalIdentifiers()) {
            if (KBO_BE.equals(extIdentifier.getInvolvedPartyExternalIdentifierType())) {
                return Optional.of(extIdentifier.getInvolvedPartyExternalIdentifierValue());
            }
        }
        return Optional.empty();
    }
}

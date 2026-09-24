package com.ing.bankguarantees.remote.rest.referencedata.multilingual.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.referencedata.ReferenceDataProperties;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Create Request to get country names translations from Reference Data API
 */
@Slf4j
@Component
@EqualsAndHashCode(callSuper = false)
public class ReferenceDataMultilingualReqTransformer extends RequestTransformer<Void> {

    public static final String TABLE_DISTRIBUTION_NAME = "tableDistributionName";
    public static final String LANGUAGE = "language";
    private final ReferenceDataProperties properties;

    public ReferenceDataMultilingualReqTransformer(
            @Value("${rest.reference-data.multilingual.ctry-api-url}") @NotNull String urlFormat,
            ReferenceDataProperties properties) {
        super(urlFormat);
        this.properties = properties;
    }


    @Override
    public Request transform(Void aVoid) {
        log.info("ReferenceDataMultilingualReqTransformer [transform]  call tableDistributionName : {} ", properties.getTableDistributionName());
        Request request = new RichHttpRequestBuilder()
                .withMethod(Method.Get())
                .withParam(TABLE_DISTRIBUTION_NAME, properties.getTableDistributionName())
                .addQueryParam(LANGUAGE, String.join(",", properties.getLanguage()))
                .withUrl(getUrlFormat().formatted(getUrlFormat())).build();

        log.info("ReferenceDataMultilingualReqTransformer [transform] : Calling GET {} endpoint[{}] ", getUrlFormat(), request.hashCode());
        return request;
    }
}

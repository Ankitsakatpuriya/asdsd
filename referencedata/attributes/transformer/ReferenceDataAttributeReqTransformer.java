package com.ing.bankguarantees.remote.rest.referencedata.attributes.transformer;


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


@Slf4j
@Component
@EqualsAndHashCode(callSuper = false)
public class ReferenceDataAttributeReqTransformer extends RequestTransformer<Void> {

    public static final String TABLE_DISTRIBUTION_NAME = "tableDistributionName";
    public static final String COLUMN = "column";
    private final ReferenceDataProperties properties;

    public ReferenceDataAttributeReqTransformer(
            @Value("${rest.reference-data.attribute.ctry-api-url}") @NotNull String urlFormat,
            ReferenceDataProperties properties) {
        super(urlFormat);
        this.properties = properties;
    }


    @Override
    public Request transform(Void aVoid) {
        log.info("ReferenceDataAttributeReqTransformer [transform]  call tableDistributionName : {} ", properties.getTableDistributionName());
        Request request = new RichHttpRequestBuilder()
                .withMethod(Method.Get())
                .withParam(TABLE_DISTRIBUTION_NAME, properties.getTableDistributionName())
                .addQueryParam(COLUMN, String.join(",", properties.getColumns()))
                .withUrl(getUrlFormat().formatted(getUrlFormat())).build();

        log.info("ReferenceDataAttributeReqTransformer [transform] : Calling GET {} endpoint[{}] ", getUrlFormat(), request.hashCode());
        return request;
    }
}

package com.ing.bankguarantees.remote.rest.masterapireference.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.masterapireference.MasterReferenceProperties;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.ing.bankguarantees.utils.JsonUtils.getJsonFromObject;


@Slf4j
@Component
public class MasterReferenceReqTransformer extends RequestTransformer<Void> {

    private static final String BRANCH_ID = "branchId";
    private static final String PRODUCT_ID = "productId";
    private final MasterReferenceProperties properties;

    public MasterReferenceReqTransformer(@Value("${rest.master-api-reference-url}") String urlFormat,
                                         MasterReferenceProperties masterreferenceProperties) {
        super(urlFormat);
        this.properties = masterreferenceProperties;
    }

    @Override
    public Request transform(Void aVoid) {
        log.info("Master API Reference [transform]  call ");
        Request request = new RichHttpRequestBuilder()
                .withUrl(String.format(getUrlFormat()))
                .withMethod(Method.Get())
                .withParam(BRANCH_ID, properties.getBranchId())
                .withParam(PRODUCT_ID, properties.getProductId())
                .build();
        log.info(C3LogMarker.marker, "Master API Reference : Calling POST {} endpoint[{}] for {}", getUrlFormat(), request.hashCode(),
                getJsonFromObject(properties.getBranchId()));
        return request;
    }
}

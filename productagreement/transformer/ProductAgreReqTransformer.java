package com.ing.bankguarantees.remote.rest.productagreement.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.productagreement.ProductAgreementProperties;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EqualsAndHashCode(callSuper = false)
public class ProductAgreReqTransformer extends RequestTransformer<String> {


    private final ProductAgreementProperties properties;

    public ProductAgreReqTransformer(@Value("${rest.product-agreement.url}") String urlFormat, ProductAgreementProperties properties) {
        super(urlFormat);
        this.properties = properties;
    }

    @Override
    public Request transform(String organisationId) {
        Request request = new RichHttpRequestBuilder()
                .withMethod(Method.Get())
                .withUrl(getUrlFormat())
                .addQueryParam("involvedPartyId", organisationId)
                .addQueryParam("productType", String.join(",", properties.getProductTypes()))
                .build();
        log.info("ProductAgreementsAPI: Calling GET {} endpoint[{}] for organisation id {}", getUrlFormat(), request.hashCode(),
                organisationId);
        return request;
    }
}

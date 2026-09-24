package com.ing.bankguarantees.remote.rest.ccaas.getdocument.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class DocumentRequestTransformer extends RequestTransformer<String> {


    public DocumentRequestTransformer(@Value("${rest.gcc.case-management.document.content.url}") @NotNull String urlFormat) {
        super(urlFormat);
    }

    @Override
    public Request transform(String input) {
        Request request = new RichHttpRequestBuilder()
                .withMethod(Method.Get())
                .withUrl(getUrlFormat())
                .withParam("documentId", input)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        log.info("CommonCoreAPI: Calling GET {} endpoint[{}]", getUrlFormat(), request.hashCode());
        return request;
    }
}

package com.ing.bankguarantees.remote.rest.permission.transformer;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.trust.token.Names;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.permission.model.request.PermissionRequest;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Requester transformer to transform the request
 */

@Slf4j
@Component
public class PermissionRequestTransformer extends RequestTransformer<PermissionRequest> {

    private static final String PATH_PARAM_ID = "id";
    private static final String SERVICE_ACTIVITY_CODE = "serviceActivityTypes";
    private final String serviceActivityName;

    public PermissionRequestTransformer(@Value("${rest.permission-be-api-involveparty-url}") String urlFormat,
                                        @Value("${bgos.service-activity-type}") String serviceActivityName) {
        super(urlFormat);
        this.serviceActivityName = serviceActivityName;
    }


    @Override
    public Request transform(PermissionRequest permissionRequest) {

        AccessToken accessToken = permissionRequest.getAccessToken();
        Request request = new RichHttpRequestBuilder()
                .withMethod(Method.Get())
                .withUrl(String.format(getUrlFormat()))
                .withHeader(Names.AccessTokenHeaderName(), accessToken.serialize())
                .withParam(PATH_PARAM_ID, permissionRequest.getParamValue())
                .withQueryParam(SERVICE_ACTIVITY_CODE, serviceActivityName)
                .build();
        log.info("Calling  GET {} endpoint[{}] for individual profileId {}", getUrlFormat(), request.hashCode(),
                accessToken.getClaimsSet().getExecutor().getProfile());
        return request;

    }

}

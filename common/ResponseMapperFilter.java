package com.ing.bankguarantees.remote.common;

import com.ing.apisdk.toolkit.connectivity.filter.ResponseTransformingFilter;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

/**
 * Filter that maps response to a {@link Class}
 * <p> Jackson exceptions are cast to {@link BgosException}
 */
@Slf4j
public class ResponseMapperFilter<T> extends ResponseTransformingFilter<Request, T, Response> {

    public ResponseMapperFilter( Class<T> resultType,  ObjectMapper objectMapper) {
        super(res -> ResponseMapperFilter.mapResponse(res, resultType, objectMapper));
    }

    private static <T> T mapResponse( Response response,  Class<T> resultType,
                                      ObjectMapper objectMapper) {

        try {
            return objectMapper.readValue(response.getContentString(), resultType);
        } catch ( Exception ex) {
            log.error("fail to parse response. Invalid response from client with message  : {}", ex.getMessage());
            throw new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT, ex);
        }
    }


}

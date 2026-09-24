package com.ing.bankguarantees.remote.common;

import com.ing.apisdk.toolkit.connectivity.filter.ResponseTransformingFilter;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.error.logger.ExceptionLogger;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


@Slf4j
public class PdfResponseMapperFilter<T> extends ResponseTransformingFilter<Request, T, Response> {

    public PdfResponseMapperFilter( Class<T> resultType) {
        super(res -> PdfResponseMapperFilter.mapResponse(res, resultType));
    }

    private static <T> T mapResponse( Response response, Class<T> resultType) {
        try {
            return (T) response.getInputStream().readAllBytes();
        } catch (IOException e) {
            ExceptionLogger.error(e, "Could not read the pdf file as byte array {}", resultType.getName());
            throw new BgosException(ErrorCode.PDF_READ_ERROR);
        }
    }
}

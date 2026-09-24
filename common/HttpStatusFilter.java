package com.ing.bankguarantees.remote.common;

import com.ing.apisdk.toolkit.connectivity.transport.core.finagle.filter.ResponseClassifiedAsFailure;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.config.ErrorDataMapper;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.twitter.finagle.Failure;
import com.twitter.finagle.Service;
import com.twitter.finagle.SimpleFilter;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.finagle.http.Status;
import com.twitter.util.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import scala.PartialFunction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Filter that logs and maps {@link HttpStatus}
 */
@Slf4j
@RequiredArgsConstructor
public class HttpStatusFilter extends SimpleFilter<Request, Response> {

    private static final FinagleErrorHandler finagleErrorHandler = new FinagleErrorHandler();
    private final ErrorDataMapper errorDataMapper;

    /**
     * Log and map {@link HttpStatus} to {@link ClientException}
     *
     * @param request request data
     * @param service service making the request
     * @throws ClientException if error from 3rd party
     * @throws BgosException   if deserialization problem
     */
    @Override
    public Future<Response> apply(Request request, Service<Request, Response> service) {
        log.info("Calling {}: {} [{}]", request.method(), request.uri(), request.hashCode());
        return service.apply(request)
                .handle(finagleErrorHandler)
                .filter(response -> filterResponseStatus(request, response));
    }

    private boolean filterResponseStatus(Request request, Response response) {
        HttpStatus httpStatus = HttpStatus.valueOf(response.statusCode());
        log.info("Received response for requestId [{}] with status from API {} {} : {}({}) {}",
                request.hashCode(), request.method(), request.path(), httpStatus.name(), httpStatus.value(), response.contentString());
        if (httpStatus.isError() || httpStatus.equals(HttpStatus.NO_CONTENT)) {
            handleClientExceptions(request, response);
        } else if (APPLICATION_JSON_VALUE.equals(response.mediaType().get())) {
            log.info(C3LogMarker.marker, "Received response for requestId [{}] with data from API {} {} : {}, status {}",
                    request.hashCode(), request.method(), request.path(), response.getContentString(), response.status());
        } else {
            log.info(C3LogMarker.marker, "Received response for requestId [{}] with mediaType from API {} {} : {}",
                    request.hashCode(), request.method(), request.path(), response.mediaType().get());
        }
        log.info(C3LogMarker.marker, "Received response for requestId [{}] with status from API {} {} : {}",
                request.hashCode(), request.method(), request.path(), response.status());
        return true;
    }

    private void handleClientExceptions(Request request, Response response) {
        log.error("Received error for requestId [{}], method {}, path {}, status code {} with response: {} ", request.hashCode(),
                request.method(), request.uri(), response.statusCode(), response.getContentString());
        log.error(C3LogMarker.marker, "Received error for requestId [{}], method {}, path {}, status code {} with response: {} ",
                request.hashCode(), request.method(), request.uri(), response.statusCode(), response.getContentString());

        ErrorItem errorItem = errorDataMapper.getErrorItemByInternalHttpStatus(response.statusCode());
        throw new ClientException(errorItem.getCode(), errorDataMapper.getErrorSource(), errorItem);
    }

    private static class FinagleErrorHandler implements PartialFunction<Throwable, Response> {

        private static final Pattern PATTERN = Pattern.compile("Status\\((\\d*)\\)");

        @Override
        public boolean isDefinedAt(Throwable error) {
            return true;
        }

        @Override
        public Response apply(Throwable error) {
            log.error("Received error with message : {}", error.getMessage(), error);
            Matcher matcher = FinagleErrorHandler.PATTERN.matcher(error.getMessage());
            Response response = null;
            if (error.getCause() instanceof ResponseClassifiedAsFailure) {
                response = (Response) ((ResponseClassifiedAsFailure) error.getCause()).response();
            }
            if (matcher.find()) {
                int status = Integer.parseInt(matcher.group(1));
                if (HttpStatus.resolve(status) != null) {
                    return response != null ? response.status(Status.fromCode(status)) : Response.apply(Status.fromCode(status));
                }
            }
            if (error instanceof Failure failure) {

                log.error("Received Timeout error with message : {}, sources : {}", failure.getMessage(),
                        failure.sources());
            }
            return response != null ? response.status(Status.RequestTimeout()) : Response.apply(Status.RequestTimeout());
        }
    }
}

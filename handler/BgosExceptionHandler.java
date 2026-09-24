package com.ing.bankguarantees.handler;


import com.ing.apisdk.toolkit.esperanto.core.*;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.HttpErrorResolver;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.error.logger.ExceptionLogger;
import com.ing.bankguarantees.error.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;


/**
 * Class that maps errors to an {@link EsperantoError}
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class BgosExceptionHandler {

    private final HttpErrorResolver errorResolver;


    /**
     * Converts the {@code exception} to an {@link EsperantoError}
     *
     * @param exception {@link } thrown by the app
     * @param request   http request
     * @param response  http response
     * @return ErrorResponse
     */
    @ExceptionHandler({BgosException.class})
    public ResponseEntity<ErrorResponse> handleBgosException(BgosException exception, HttpServletRequest request,
                                                             HttpServletResponse response) {
        log.error("BGOSExceptionHandler [handleBgosException] call");
        ExceptionLogger.error(exception, "Received a BgosException with message {}", exception.getMessage());
        ErrorItem errorItem = errorResolver.resolve(exception.getErrorCode().getCode(), ErrorSource.BGOS);
        return new ResponseEntity<>(ErrorResponse.newErrorResponse(errorItem), errorItem.getExternalHttpStatus());
    }

    /**
     * Returns for the {@code exception} the payload {@link ErrorResponse}
     * and the status configured in the externalHttpStatus
     * Instr
     *
     * @param exception {@link ClientException} thrown by the app
     * @return {@link ResponseEntity} error response
     */
    @ExceptionHandler({ClientException.class})
    public ResponseEntity<ErrorResponse> handleClientException(ClientException exception, HttpServletRequest request) {
        log.error("BGOSExceptionHandler [handleClientException] call");
        ExceptionLogger.error(exception, "Received a ClientException with message {}", exception.getMessage());
        ErrorItem errorItem = exception.getErrorItem();
        return new ResponseEntity<>(ErrorResponse.newErrorResponse(errorItem), errorItem.getExternalHttpStatus());
    }

    /**
     * Converts the {@code exception} to an {@link EsperantoError}
     *
     * @param exception {@link ConstraintViolationException} thrown by the app
     * @param request   http request
     * @param response  http response
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public void handleViolationException(ConstraintViolationException exception, HttpServletRequest request,
                                         HttpServletResponse response) {
        log.error("BGOSExceptionHandler [handleViolationException] call");
        ExceptionLogger.error(exception, "Received a ConstraintViolationException with message {}", exception.getMessage());
        String validationMessage = exception.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        String codeError = ErrorCode.INVALID_REQUEST.getCode();
        ErrorItem errorItem = errorResolver.resolve(codeError, ErrorSource.BGOS);
        HttpEsperantoError error;
        String errorMessage = errorItem.getMessage();
        if (StringUtils.isNotEmpty(validationMessage)) {
            errorMessage = String.format("%s  %s", errorMessage, validationMessage);
        }
        error = HttpEsperantoError.apply(
                new EsperantoError(codeError, errorMessage)
                        .withSeverity(Severity.valueOf(errorItem.getSeverity().toUpperCase())),
                ErrorType.FUNCTIONAL, errorItem.getExternalHttpStatus().value());

        EsperantoErrorHelper.setError(request, response, error);
    }

    /**
     * Converts the {@code exception} to an {@link EsperantoError}
     *
     * @param exception {@link MethodArgumentNotValidException} thrown by the app
     * @param request   http request
     * @param response  http response
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public void handleViolationException(MethodArgumentNotValidException exception, HttpServletRequest request,
                                         HttpServletResponse response) {
        log.error("BGOSExceptionHandler [handleMethodArgumentValidationException] call");
        ExceptionLogger.error(exception, "Received a ConstraintViolationException with message {}", exception.getMessage());
        String validationMessage = exception.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.error(" Error message : {}", validationMessage);

        String codeError = ErrorCode.INVALID_REQUEST.getCode();
        ErrorItem errorItem = errorResolver.resolve(codeError, ErrorSource.BGOS);
        HttpEsperantoError error;
        String errorMessage = errorItem.getMessage();
        if (StringUtils.isNotEmpty(validationMessage)) {
            errorMessage = String.format("%s  %s", errorMessage, validationMessage);
        }
        error = HttpEsperantoError.apply(
                new EsperantoError(codeError, errorMessage)
                        .withSeverity(Severity.valueOf(errorItem.getSeverity().toUpperCase())),
                ErrorType.FUNCTIONAL, errorItem.getExternalHttpStatus().value());

        EsperantoErrorHelper.setError(request, response, error);
    }

    /**
     * @param exception exception thrown by the service
     * @param request   http request
     * @param response  http response
     */
    @ExceptionHandler({Exception.class})
    public void handleException(Exception exception, HttpServletRequest request,
                                HttpServletResponse response) {

        log.error("BGOSExceptionHandler [handleException] call");
        ExceptionLogger.error(exception, "Received a Exception with message {}", exception.getMessage());
        String codeError = ErrorCode.TECHNICAL_ERROR.getCode();
        ErrorItem errorItem = errorResolver.resolve(codeError, ErrorSource.BGOS);
        HttpEsperantoError error = HttpEsperantoError.apply(
                new EsperantoError(codeError, errorItem.getMessage())
                        .withSeverity(Severity.valueOf(errorItem.getSeverity().toUpperCase())),
                ErrorType.TECHNICAL, errorItem.getExternalHttpStatus().value());
        EsperantoErrorHelper.setError(request, response, error);
    }


}

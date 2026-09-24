package com.ing.bankguarantees.error.logger;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;

/**
 * Logger for Exceptions
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionLogger {

    private static final String STACK_TRACE = "STACK TRACE";

    /**
     * @param exception    BLOS Exception
     * @param errorMessage Error Message
     * @param arguments    Arguments to be replaced in error message
     */
    public static void error(Exception exception, String errorMessage, Object... arguments) {
        log.error(errorMessage, arguments);
        log.error(STACK_TRACE, exception);
    }

    /**
     * @param exception    Throwable
     * @param errorMessage Error Message
     * @param arguments    Arguments to be replaced in error message
     */
    public static void error(Throwable exception, String errorMessage, Object... arguments) {
        log.error(errorMessage, arguments);
        log.error(STACK_TRACE, exception);
    }

    /**
     * @param exception    BLOS Exception
     * @param marker       Marker, can be used for C3 data
     * @param errorMessage Error Message
     * @param arguments    Arguments to be replaced in error message
     */
    public static void error(Throwable exception, Marker marker, String errorMessage, Object... arguments) {
        log.error(marker, errorMessage, arguments);
        log.error(marker, STACK_TRACE, exception);
    }

}

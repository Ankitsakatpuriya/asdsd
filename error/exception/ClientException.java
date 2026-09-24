package com.ing.bankguarantees.error.exception;

import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import lombok.Getter;


/**
 * <p> Base class for all exceptions that should be thrown
 * when handling responses from Finagle(Rest) clients </p>
 */

@Getter
public class ClientException extends RuntimeException {

    private final transient ErrorItem errorItem;
    private final ErrorSource errorSource;

    /**
     * @param message     error code found in json error files
     * @param errorSource external API that thrown the error
     * @param errorItem   Error item
     */
    public ClientException(String message, ErrorSource errorSource, ErrorItem errorItem) {
        super(message);
        this.errorItem = errorItem;
        this.errorSource = errorSource;
    }

}

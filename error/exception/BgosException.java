package com.ing.bankguarantees.error.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;


/**
 * Base class for all exceptions thrown within the BE
 */
@Getter
@Slf4j
public class BgosException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -5580224935402162553L;
    private final ErrorCode errorCode;

    /**
     * @param errorCode Error code
     */
    public BgosException(ErrorCode errorCode) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
    }

    /**
     * @param errorCode Error code
     * @param throwable Root Exception
     */
    public BgosException(ErrorCode errorCode, Throwable throwable) {
        super(errorCode.getCode(), throwable);
        this.errorCode = errorCode;
    }
}

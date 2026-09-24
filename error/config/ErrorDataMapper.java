package com.ing.bankguarantees.error.config;

import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;


/**
 * Mapper to have list of errors by http status and by error code
 */
@Slf4j
public class ErrorDataMapper {

    @Getter
    private final ErrorSource errorSource;
    private Map<HttpStatus, ErrorItem> errorsByInternalHttpStatus;
    private Map<String, ErrorItem> errorsByCode;

    /**
     * @param loader      defines loader of json error files
     * @param errorPrefix defines error sources for all internal/external errors
     */
    public ErrorDataMapper(ErrorDataLoader loader, ErrorSource errorPrefix) {
        errorSource = errorPrefix;
        List<ErrorItem> errorItems = loader.getList(errorPrefix);
        mapErrorsByInternalHttpStatus(errorItems);
        mapErrorsByCode(errorItems);
    }

    /**
     * @param httpStatusCode http code to get the error associated with it
     * @return error item found from the json error files or by default the one mapped for the INTERNAL_SERVER_ERROR
     */
    public ErrorItem getErrorItemByInternalHttpStatus(int httpStatusCode) {
        HttpStatus httpStatus = HttpStatus.valueOf(httpStatusCode);
        ErrorItem errorItem = errorsByInternalHttpStatus.get(httpStatus);
        if (ObjectUtils.isEmpty(errorItem)) {
            log.warn("The errorItem was not found for the httpStatusCode = {}", httpStatusCode);
            errorItem = errorsByInternalHttpStatus.get(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return errorItem;
    }

    /**
     * @param code error code
     * @return error item found from the json error files or by default the one mapped for the INTERNAL_SERVER_ERROR
     */
    public ErrorItem getErrorItemByCode(String code) {
        ErrorItem errorItem = errorsByCode.get(code);
        if (errorItem == null) {
            log.error("The errorItem was not found for the code = {}}", code);
            return errorsByInternalHttpStatus.get(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return errorItem;
    }

    private void mapErrorsByInternalHttpStatus(List<ErrorItem> errorItems) {
        errorsByInternalHttpStatus = errorItems.stream()
                .filter(errorItem -> errorItem.getInternalHttpStatus() != null)
                .collect(toMap(ErrorItem::getInternalHttpStatus, identity(),
                        (l, r) -> {
                            throw new IllegalArgumentException("Duplicate keys " + l + "and " + r + ".");
                        })
                );
        if (!errorsByInternalHttpStatus.containsKey(HttpStatus.INTERNAL_SERVER_ERROR)) {
            throw new IllegalArgumentException("HttpStatus INTERNAL_SERVER_ERROR is not mapped for error mapper for source "
                    + errorSource.getPrefix());
        }
    }

    private void mapErrorsByCode(List<ErrorItem> errorItems) {
        errorsByCode = errorItems.stream().collect(Collectors.toMap(ErrorItem::getCode, identity()));
    }
}

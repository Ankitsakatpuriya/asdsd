package com.ing.bankguarantees.error.config;


import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Mapper between error code and the error item (item from json error file)
 */
@Component
@RequiredArgsConstructor
public class HttpErrorResolver {

    private final Map<ErrorSource, ErrorDataMapper> errorDataMapperMap;

    /**
     * Returns the error item from the error mapper for that source.
     * If there is no error mapper for that source the BusinessLendingApplication error mapper
     * will be used, from which the error item for HttpStatus.INTERNAL_SERVER_ERROR will be used.
     *
     * @param code        error code
     * @param errorSource source system
     * @return error item
     */
    public ErrorItem resolve(String code, ErrorSource errorSource) {
        ErrorDataMapper errorDataMapper = errorDataMapperMap.get(errorSource);
        if (errorDataMapper != null) {
            return errorDataMapper.getErrorItemByCode(code);
        }
        ErrorDataMapper defaultMapper = errorDataMapperMap.get(ErrorSource.BGOS);
        return defaultMapper.getErrorItemByInternalHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
package com.ing.bankguarantees.remote.common;


import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Set;

import static java.util.stream.Collectors.joining;


@Slf4j
@AllArgsConstructor
public class ResponseValidator<T> {

    private final Validator validator;


    public T validate(T response) {
        Set<ConstraintViolation<Object>> violations = validator.validate(response);
        if (CollectionUtils.isNotEmpty(violations)) {
            log.error("{}  found with the {}  violation(s)  --> {}  ",
                    response.getClass().getSimpleName(),
                    violations.size(),
                    violations.stream().map(objectConstraintViolation -> objectConstraintViolation.getPropertyPath() + " "
                            + objectConstraintViolation.getMessage()).collect(joining(" :: ")));
            throw new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
        }
        return response;
    }
}

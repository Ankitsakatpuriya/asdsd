package com.ing.bankguarantees.util;


import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.assertj.core.api.Assertions.assertThat;


public class ResponseBodyMatchers {

    public static ResponseBodyMatchers responseBody() {
        return new ResponseBodyMatchers();
    }

    public <T> ResultMatcher containsObjectAsJson(Object expectedObject, Class<T> targetClass) {
        return mvcResult -> {
            T actualObject = targetClass.cast(((ResponseEntity<?>) mvcResult.getAsyncResult()).getBody());
            assertThat(actualObject).isEqualTo(expectedObject);
        };
    }


}

package com.ing.bankguarantees.error.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import lombok.*;


/**
 * Class matching error response body
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ErrorResponse {

    @JsonProperty("error")
    private ErrorBody error;

    /**
     * @param errorItem item matched in the error json files
     * @return error response
     */
    public static ErrorResponse newErrorResponse(ErrorItem errorItem) {

         ErrorBody errorBody = ErrorBody.builder()
                .severity(errorItem.getSeverity().toLowerCase())
                .code(errorItem.getCode())
                .message(errorItem.getMessage())
                .build();
        return ErrorResponse.builder().error(errorBody).build();

    }
}


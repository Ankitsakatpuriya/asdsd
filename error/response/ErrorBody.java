package com.ing.bankguarantees.error.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * ErrorBody defines the structure of Error message Response when exception occurred
 */
@SuperBuilder
@NoArgsConstructor
@Data
public class ErrorBody {

    @JsonProperty("severity")
    private String severity;

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;


}

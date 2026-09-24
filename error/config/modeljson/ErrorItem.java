package com.ing.bankguarantees.error.config.modeljson;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErrorItem {
    private String code;
    private String message;
    private String severity;
    private String source;
    /**
     * Http status to be returned by the Business Lending Applications
     */
    @JsonDeserialize(using = HttpStatusDeserializer.class)
    private HttpStatus externalHttpStatus;
    /**
     * Http status returned by the external APIs
     */
    @JsonDeserialize(using = HttpStatusDeserializer.class)
    private HttpStatus internalHttpStatus;

}

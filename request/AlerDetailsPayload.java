package com.ing.bankguarantees.models.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlerDetailsPayload {

    @NotBlank
    private String transactionId;
    private boolean invokeAler;
    private List<String> signers;


}
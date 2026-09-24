
package com.ing.bankguarantees.remote.rest.permission.model.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;


/**
 * InvolvedParty
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Validated
public class PermissionResponse {

    @NotBlank
    private String identifier;

    @NotBlank
    private String type;

    @Valid
    @NotNull
    private List<ServiceActivity> serviceActivities;


}


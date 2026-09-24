package com.ing.bankguarantees.remote.rest.permission.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceActivity {
    @NotNull
    private String type;

}
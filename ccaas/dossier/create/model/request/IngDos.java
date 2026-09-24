package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Common Class for CommonCore API payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngDos {
    private String code;
}
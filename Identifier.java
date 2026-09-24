package com.ing.bankguarantees.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Class to store UUID and IBAN Value
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Identifier {
    private String type;
    private String value;
}

package com.ing.bankguarantees.remote.rest.productagreement.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
@Data
@AllArgsConstructor
@Builder
public class ProductAgreementAccount {

    private String productType;
    private String uuid;
    private String iban;
    private String creditLineAccountNumber;
    private String accountName;
    private String currency;
    private String bePan;

}

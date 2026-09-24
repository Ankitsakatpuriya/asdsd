package com.ing.bankguarantees.remote.rest.dar.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationCodeRequest {

    private Integer translationCode;

}


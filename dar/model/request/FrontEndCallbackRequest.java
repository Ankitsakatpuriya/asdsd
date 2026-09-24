package com.ing.bankguarantees.remote.rest.dar.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontEndCallbackRequest {
    private String redirectUrl;
    private String cancelUrl;
    private String closeUrl;
    private TranslationCodeRequest redirectText;
    private List<String> exemptActorIds;
}

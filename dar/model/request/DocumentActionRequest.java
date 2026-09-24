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
public class DocumentActionRequest {

    private String name;
    private DocumentActionType type;
    private List<String> dependsOn;
    private String parentName;
    private Boolean main;
    private String signingMean;
    private String actorId;
    private String documentUri;
    private TranslationCodeRequest documentName;
    private Boolean mandatory;
    private String documentLanguage;
    private List<LegalNoticeRequest> legalNotices;
    private TranslationCodeRequest signatoryNotice;
    private SignatoryType signatoryType;
    private Integer signatoryPosition;

}


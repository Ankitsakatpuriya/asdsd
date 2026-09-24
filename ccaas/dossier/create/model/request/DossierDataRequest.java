package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ing.bankguarantees.utils.ConstantUtils;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class DossierDataRequest {

    private static final String TENANT_OWNER_ID = "T_BE001";
    private static final String APPEARANCE_OWNER_ID = "A_GS001";

    @Builder.Default
    @JsonProperty("ing_tenant_owner_id")
    private String ingTenantOwnerId = TENANT_OWNER_ID;

    @Builder.Default
    @JsonProperty("ing_appearance_owner_id")
    private String ingAppearanceOwnerId = APPEARANCE_OWNER_ID;

    @Builder.Default
    @JsonProperty("ing_application_owner_id")
    private String ingApplicationOwnerId = ConstantUtils.ING_APPLICATION_OWNER_ID;

    @Builder.Default
    @JsonProperty("ing_c_rating")
    private Integer ingCRating = 3;

    @Builder.Default
    @JsonProperty("ing_i_rating")
    private Integer ingIRating = 3;

    @Builder.Default
    @JsonProperty("ing_a_rating")
    private Integer ingARating = 3;


}

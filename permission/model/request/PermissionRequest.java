package com.ing.bankguarantees.remote.rest.permission.model.request;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.bankguarantees.models.enums.ServiceActivitiesCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequest {

    private ServiceActivitiesCode serviceActivitiesCode;
    private transient AccessToken accessToken;
    private String paramValue;

}

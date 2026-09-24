package com.ing.bankguarantees.remote.rest.permission.transformer;

import com.ing.bankguarantees.remote.rest.permission.model.request.PermissionRequest;
import com.ing.bankguarantees.models.enums.ServiceActivitiesCode;
import com.ing.bankguarantees.util.MockHelper;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class PermissionRequestTransformerTest {
    private static final String URL_FORMAT = "https://api.ing.com/v4/permissions/involved-parties/{id}";
    private static final String LE_ID = "12345";

    @Test
    void transformTest() {

        PermissionRequest permissionRequest = MockHelper.getPermissionRequest(ServiceActivitiesCode.ORGANIZATION, LE_ID);

        Request request = new PermissionRequestTransformer(URL_FORMAT,"involved-parties:bank-guarantees:create").transform(permissionRequest);
        assertThat(request.uri()).isEqualTo(String.format("/v4/permissions/involved-parties/%s?serviceActivityTypes=%s", LE_ID, "involved-parties%3Abank-guarantees%3Acreate"));
        assertThat(request.method()).isEqualTo(Method.Get());
    }
}

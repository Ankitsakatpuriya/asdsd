package com.ing.bankguarantees.remote.rest.aler.transformer;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.bankguarantees.remote.rest.aler.model.request.AlerRetrieveSignatoriesInput;
import com.ing.bankguarantees.util.MockHelper;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AlerRetrieveSignatoriesRequestTransformerTest {

    private static final String URL_FORMAT = "https://api.ing.com/representation-management-be/retrieve-signatories";
    private static final String REQUESTER_ID = "e20b9980-cbc3-4ae6-beae-77fe39eee4eb";
    private static final String UUID_ORG = "3f97bf90-9dd9-4acb-b99a-d4c49be71ece";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String PURPOSE_ID = "1013";

    @Test
    void transformTest() {
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        AlerRetrieveSignatoriesInput alerRetrieveSignatoriesInput = MockHelper.getAlerRetrieveSignatoriesInput(accessToken, UUID_ORG);
        Request request = new AlerRetrieveSignatoriesRequestTransformer(URL_FORMAT).transform(alerRetrieveSignatoriesInput);
        assertThat(request.uri()).contains("/representation-management-be/retrieve-signatories");
        assertThat(request.method()).isEqualTo(Method.Post());
        assertThat(request.getParam("purposeId")).isEqualTo(PURPOSE_ID);
        assertThat(request.getParam("transactionId")).isNotBlank();
        assertThat(request.contentString()).contains(UUID_ORG);
    }
}
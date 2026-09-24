package com.ing.bankguarantees.remote.rest.productagreement.transformer;

import com.ing.bankguarantees.remote.rest.productagreement.ProductAgreementProperties;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ProductAgreReqTransformerTest {
    private static final String URL_FORMAT = "https://apis.ing.com/v6/agreements/products";
    private static final String ORG_ID = "0325895254";

    @Mock
    private ProductAgreementProperties properties;


    @Test
    void transformTest() {

        given(properties.getProductTypes()).willReturn(Set.of("BE_ING_MGN_CRN_AC", "BE_ING_CORP_AC"));
        Request request = new ProductAgreReqTransformer(URL_FORMAT, properties).transform(ORG_ID);
        assertThat(request.uri()).contains(String.format("/v6/agreements/products?involvedPartyId=%s", ORG_ID));
        assertThat(request.method()).isEqualTo(Method.Get());
        assertThat(request.getParam("involvedPartyId")).isEqualTo(ORG_ID);
        assertThat(request.getParam("productType")).contains("BE_ING_CORP_AC");
        assertThat(request.getParam("productType")).contains("BE_ING_MGN_CRN_AC");

    }

}

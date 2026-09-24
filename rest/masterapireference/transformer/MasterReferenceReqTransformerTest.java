package com.ing.bankguarantees.remote.rest.masterapireference.transformer;

import com.ing.bankguarantees.remote.rest.masterapireference.MasterReferenceProperties;
import com.twitter.finagle.http.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class MasterReferenceReqTransformerTest {

    private static final String urlFormat = "http://localhost:8080/api/master-reference";
    private static final String BRANCH_ID = "branchId";
    private static final String PRODUCT_ID = "productId";

    @Mock
    MasterReferenceProperties mockProps;

    @Test
    void transformRequestParams() {

        given(mockProps.getProductId()).willReturn(PRODUCT_ID);
        given(mockProps.getBranchId()).willReturn(BRANCH_ID);
        MasterReferenceReqTransformer reqTransformer = new MasterReferenceReqTransformer(urlFormat, mockProps);
        Request request = reqTransformer.transform(null);
        assertTrue(request.uri().startsWith("/api/master-reference"));
    }

}

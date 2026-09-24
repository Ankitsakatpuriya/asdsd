package com.ing.bankguarantees.remote.rest.dar.transformers;


import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.remote.rest.dar.DarProperties;
import com.ing.bankguarantees.remote.rest.dar.mapper.DarV3RequestMapper;
import com.ing.bankguarantees.remote.rest.dar.model.request.DarInput;
import com.ing.bankguarantees.util.MockHelper;
import com.twitter.finagle.http.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DocumentSignV3RequestTransformerTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String URL_FORMAT = "https://api.ing.com/docsign/v2/document-action-requests";
    private static final String SIGN_MEAN = "AES";
    private static final String APPLICATION_NAME = "BankGuaranteesBEOnline_API";
    private static final String SERVICE_ACTIVITY = "involved-parties:bank-guarantees:create";
    private static final String DOCUMENT_ID = "344135tplow";


    private DarProperties darProperties;

    private DocumentSignV3RequestTransformer transformer;

    @BeforeEach
    void setUp() {
        darProperties = MockHelper.getDarProperties();
        DarV3RequestMapper darRequestMapper = new DarV3RequestMapper(darProperties);
        ReflectionTestUtils.setField(darRequestMapper, "signingMean", SIGN_MEAN);
        ReflectionTestUtils.setField(darRequestMapper, "applicationName", APPLICATION_NAME);
        ReflectionTestUtils.setField(darRequestMapper, "serviceActivity", SERVICE_ACTIVITY);
        transformer = new DocumentSignV3RequestTransformer(URL_FORMAT, darRequestMapper);
    }

    @Test
    void checkTransform() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setSignExpiryDate(LocalDate.now().plusDays(10));
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        DarInput darInput = MockHelper.getDarInput(bankGuaranteeRequestData, List.of(document));
        Request request = transformer.transform(darInput);
        assertThat(request).isNotNull();
        assertThat(request.contentString()).contains("\"referenceId\":\"f67da295-72fe-4bf2-96f3-2fd36d975a98\"");
        assertThat(request.contentString()).contains("\"link\":\"/case-management/documents/344135tplow");

    }


}

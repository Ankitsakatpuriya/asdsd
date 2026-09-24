package com.ing.bankguarantees.remote.rest.pega.transformer;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.models.enums.PegaCaseType;
import com.ing.bankguarantees.remote.rest.pega.mapper.PegaRequestMapper;
import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseInput;
import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseRequest;
import com.ing.bankguarantees.util.MockHelper;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class PegaCreateCaseReqTransformerTest {

    private static final String PEGA_REQUEST_FILE = "BGA/BGF/pega_payload.json";
    private static final String URL_FORMAT = "https://api.ing.com/prweb/PRRestService/TFSAppAPI/v1/cases";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";
    private static final String DOCUMENT_ID = "344135tplow";

    @Mock
    private PegaRequestMapper pegaRequestMapper;


    @Test
    void transformTest() {
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        PegaCreateCaseInput pegaCaseInput = MockHelper.getPegaCaseInput(document, bankGuaranteeRequest, PegaCaseType.AMEND);
        PegaCreateCaseRequest pegaPayload = MockHelper.createPegaPayload(PEGA_REQUEST_FILE);
        PegaCreateCaseReqTransformer reqTransformer = new PegaCreateCaseReqTransformer(URL_FORMAT, pegaRequestMapper);
        when(pegaRequestMapper.preparePegaRequest(any())).thenReturn(pegaPayload);
        Request request = reqTransformer.transform(pegaCaseInput);
        assertThat(request.uri()).isEqualTo("/prweb/PRRestService/TFSAppAPI/v1/cases");
        assertThat(request.method()).isEqualTo(Method.Post());
        assertThat(request.contentString()).contains("{\"caseTypeID\":\"ING-WB-TFS-Work-BankGuarantee\",");
    }


}

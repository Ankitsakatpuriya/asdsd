package com.ing.bankguarantees.remote.rest.intake.transformer;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.remote.rest.intake.IntakeApiProperties;
import com.ing.bankguarantees.remote.rest.intake.mapper.IntakeApiRequestMapper;
import com.ing.bankguarantees.remote.rest.intake.model.request.IntakeApiInput;
import com.ing.bankguarantees.remote.rest.intake.model.request.IntakeApiRequest;
import com.ing.bankguarantees.remote.rest.intake.transformers.IntakeApiRequestTransformer;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.utils.JsonUtils;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class IntakeApiRequestTransformerTest {

    private static final String INTAKE_REQUEST_FILE = "BGA/BGF/intake_api_payload.json";
    private static final String URL_FORMAT = "https://api.ing.com/bank-guarantees/applications/intake";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";
    private static final String DOCUMENT_ID = "344135tplow";
    private static final String ING_CHANNEL = "MingZBE";
    private static final String ING_CHANNEL_HEADER = "X-ING-Channel";


    @Mock
    private IntakeApiProperties intakeApiProperties;

    @Mock
    private IntakeApiRequestMapper intakeApiRequestMapper;


    @Test
    void transformTest() {

        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        IntakeApiRequest intakeApiRequest = MockHelper.createIntakeApiPayload(INTAKE_REQUEST_FILE);
        when(intakeApiRequestMapper.prepareIntakeApiRequest(any())).thenReturn(intakeApiRequest);
        when(intakeApiProperties.getIngChannel()).thenReturn(ING_CHANNEL);
        IntakeApiInput intakeInput = MockHelper.getIntakeInput(bankGuaranteeRequest, List.of(document));
        IntakeApiRequestTransformer intakeApiRequestTransformer = new IntakeApiRequestTransformer(URL_FORMAT, intakeApiRequestMapper, intakeApiProperties);
        Request request = intakeApiRequestTransformer.transform(intakeInput);
        assertThat(request.uri()).isEqualTo("/bank-guarantees/applications/intake");
        assertThat(request.method()).isEqualTo(Method.Post());
        assertThat(request.contentString()).contains(JsonUtils.getJsonFromObject(intakeApiRequest));
        assertThat(request.headerMap().get(ING_CHANNEL_HEADER).get()).isEqualTo(ING_CHANNEL);

    }


}

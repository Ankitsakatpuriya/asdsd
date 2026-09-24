package com.ing.bankguarantees.remote.rest.gess.transformer;

import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.remote.rest.gess.GessProperties;
import com.ing.bankguarantees.remote.rest.gess.model.request.GessSignInput;
import com.ing.bankguarantees.util.MockHelper;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class GessSignRequestTransformerTest {

    private static final String URL_FORMAT = "https://api.ing.com/pkbox/esign";
    private static final String REQUESTER_EMAIL = "demo@ing.com";


    @ParameterizedTest
    @MethodSource("bgCodes")
    void transformTest(BankGuaranteeCode bankGuaranteeCode) {

        byte[] bytes = MockHelper.readFile(BankGuaranteeCode.PUBLIC_CONTRACT, "en");
        GessProperties gessProperties = MockHelper.getGessProperties();
        String base64String = Base64.getEncoder().encodeToString(bytes);
        GessSignInput gessSignInput = MockHelper.geGessSignInput(REQUESTER_EMAIL, base64String, DocumentType.BG_FINAL, bankGuaranteeCode);
        Request request = new GessSignRequestTransformer(URL_FORMAT, gessProperties).transform(gessSignInput);
        assertThat(request.uri()).isEqualTo("/pkbox/esign");
        assertThat(request.method()).isEqualTo(Method.Post());
        assertThat(request.contentString()).contains(base64String);

    }

    private static Stream<BankGuaranteeCode> bgCodes() {
        return Stream.of(BankGuaranteeCode.values());
    }
}


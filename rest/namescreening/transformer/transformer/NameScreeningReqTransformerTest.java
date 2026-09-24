package com.ing.bankguarantees.remote.rest.namescreening.transformer.transformer;

import com.ing.bankguarantees.remote.rest.namescreening.NameScreeningProperties;
import com.ing.bankguarantees.remote.rest.namescreening.transformer.NameScreeningReqTransformer;
import com.ing.bankguarantees.util.MockHelper;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class NameScreeningReqTransformerTest {
    private static final String URL_FORMAT = "https://api.ing.com/rtns/screen";
    @Mock
    private static NameScreeningProperties nameScreeningProperties;


    @Test
    void transformTest() {
        Request request = new NameScreeningReqTransformer(URL_FORMAT, nameScreeningProperties).transform(MockHelper.getNameScreeningInput());
        assertThat(request.uri()).isEqualTo("/rtns/screen");
        assertThat(request.method()).isEqualTo(Method.Post());
        assertThat(request.contentString()).contains("\"party\":{\"organizationName\":\"mock org name\",\"address\":{\"street\":\"mock street\",\"houseNumber\":\"mock number\",\"zipCode\":\"mock zip code\",\"city\":\"mock city\",\"countryCode\":\"mock country code\"}}");
    }

}
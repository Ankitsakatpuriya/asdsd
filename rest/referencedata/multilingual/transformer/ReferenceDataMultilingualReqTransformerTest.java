package com.ing.bankguarantees.remote.rest.referencedata.multilingual.transformer;

import com.ing.bankguarantees.remote.rest.referencedata.ReferenceDataProperties;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ReferenceDataMultilingualReqTransformerTest {
    private static final String URL_FORMAT = "https://api.referencedata.ing.net/v2/reference-data/multilingual/{tableDistributionName}";
    public static final String TABLE_DISTRIBUTION_NAME = "ctry";
    private ReferenceDataProperties properties;
    public static final List<String> LANGUAGES = List.of("FR", "EN", "NL","DE");

    @BeforeEach
    void init() {
        properties = new ReferenceDataProperties();
        properties.setTableDistributionName(TABLE_DISTRIBUTION_NAME);
        properties.setLanguage(LANGUAGES);
    }

    @Test
    void transformTest() {
        Request request = new ReferenceDataMultilingualReqTransformer(URL_FORMAT, properties).transform(null);
        assertThat(request.uri()).isEqualTo(String.format("/v2/reference-data/multilingual/%s?language=%s", TABLE_DISTRIBUTION_NAME, "FR%2CEN%2CNL%2CDE"));
        assertThat(request.method()).isEqualTo(Method.Get());
    }

}

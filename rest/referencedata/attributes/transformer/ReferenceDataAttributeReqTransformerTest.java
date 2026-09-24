package com.ing.bankguarantees.remote.rest.referencedata.attributes.transformer;

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
class ReferenceDataAttributeReqTransformerTest {
    private static final String URL_FORMAT = "https://api.referencedata.ing.net/v2/reference-data/attributes/{tableDistributionName}";
    public static final String TABLE_DISTRIBUTION_NAME = "ctry";
    public static final List<String> COLUMNS = List.of("END_DT", "INDPNDNT_IND", "FEC_RSK_TP_CODE");

    private ReferenceDataProperties properties;

    @BeforeEach
    void init() {
        properties = new ReferenceDataProperties();
        properties.setTableDistributionName(TABLE_DISTRIBUTION_NAME);
        properties.setColumns(COLUMNS);
    }

    @Test
    void transformTest() {
        Request request = new ReferenceDataAttributeReqTransformer(URL_FORMAT, properties).transform(null);
        assertThat(request.uri()).isEqualTo(String.format("/v2/reference-data/attributes/%s?column=%s", TABLE_DISTRIBUTION_NAME, "END_DT%2CINDPNDNT_IND%2CFEC_RSK_TP_CODE"));
        assertThat(request.method()).isEqualTo(Method.Get());
    }

}

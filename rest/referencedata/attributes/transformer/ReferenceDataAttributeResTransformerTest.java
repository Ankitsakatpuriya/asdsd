package com.ing.bankguarantees.remote.rest.referencedata.attributes.transformer;

import com.ing.bankguarantees.remote.rest.referencedata.attributes.model.response.ReferenceDataAttributeOutput;
import com.ing.bankguarantees.remote.rest.referencedata.attributes.model.response.ReferenceDataAttributeResponse;
import com.ing.bankguarantees.remote.rest.referencedata.attributes.model.response.ReferenceDataAttributeResponse.AttributeColumnDataResponse;
import com.ing.bankguarantees.remote.rest.referencedata.attributes.model.response.ReferenceDataAttributeResponse.AttributeDataResponse;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceDataAttributeResTransformerTest {

    private static final String COUNTRY_ATTRIBUTES_RESPONSE = "BGA/CLD/country_attribute_response.json";
    private final static String BE_BUSINESS_KEY = "BE";
    private final static String PT_BUSINESS_KEY = "PT";
    private final static String FEC_RISK_COLUMN = "fec_rsk_tp_code";
    private final static String FEC_RISK_VALUE = "ULTRA_HIGH";
    private final static String INDEPENDENT_INDICATOR_COLUMN = "indpndnt_ind";
    private final static String INDEPENDENT_INDICATOR_VALUE_NEG = "N";

    @Test
    void transformTestPositive() {
        ReferenceDataAttributeResponse countryAttributeResponse = MockHelper.createCountryAttributeResponse(COUNTRY_ATTRIBUTES_RESPONSE);
        ReferenceDataAttributeOutput response = new ReferenceDataAttributeResTransformer().transform(countryAttributeResponse);
        assertThat(response).isNotNull();
        assertThat(response.getBusinessKeyList()).isNotEmpty();
        assertThat(response.getBusinessKeyList()).containsAll(List.of(BE_BUSINESS_KEY, PT_BUSINESS_KEY));
    }

    @Test
    void transformEndDateNotNullTest() {
        ReferenceDataAttributeResponse countryAttributeResponse = MockHelper.createCountryAttributeResponse(COUNTRY_ATTRIBUTES_RESPONSE);
        AttributeDataResponse attributeDataResponse = countryAttributeResponse.getData().stream().filter(data -> data.getBusinessKey().equals(BE_BUSINESS_KEY)).findFirst().orElse(AttributeDataResponse.builder().build());
        attributeDataResponse.setEndDate(LocalDate.now());
        ReferenceDataAttributeOutput response = new ReferenceDataAttributeResTransformer().transform(countryAttributeResponse);
        assertThat(response).isNotNull();
        assertThat(response.getBusinessKeyList()).isNotEmpty();
        assertThat(response.getBusinessKeyList()).containsAll(List.of(PT_BUSINESS_KEY));
    }

    @Test
    void transformIndependentIndicatorColumnNegativeTest() {
        ReferenceDataAttributeResponse countryAttributeResponse = MockHelper.createCountryAttributeResponse(COUNTRY_ATTRIBUTES_RESPONSE);
        setColumn(countryAttributeResponse.getData(), BE_BUSINESS_KEY, INDEPENDENT_INDICATOR_COLUMN, INDEPENDENT_INDICATOR_VALUE_NEG);
        ReferenceDataAttributeOutput response = new ReferenceDataAttributeResTransformer().transform(countryAttributeResponse);
        assertThat(response).isNotNull();
        assertThat(response.getBusinessKeyList()).isNotEmpty();
        assertThat(response.getBusinessKeyList()).containsAll(List.of(PT_BUSINESS_KEY));
    }

    @Test
    void transformRiskColumnNegativeTest() {
        ReferenceDataAttributeResponse countryAttributeResponse = MockHelper.createCountryAttributeResponse(COUNTRY_ATTRIBUTES_RESPONSE);
        setColumn(countryAttributeResponse.getData(), PT_BUSINESS_KEY, FEC_RISK_COLUMN, FEC_RISK_VALUE);
        ReferenceDataAttributeOutput response = new ReferenceDataAttributeResTransformer().transform(countryAttributeResponse);
        assertThat(response).isNotNull();
        assertThat(response.getBusinessKeyList()).isNotEmpty();
        assertThat(response.getBusinessKeyList()).containsAll(List.of(BE_BUSINESS_KEY));
    }

    private void setColumn(List<AttributeDataResponse> attributeDataResponseList, String businessKey, String name, String value) {
        for (AttributeDataResponse attributeDataResponse : attributeDataResponseList) {
            if (attributeDataResponse.getBusinessKey().equals(businessKey)) {
                List<AttributeColumnDataResponse> columnDataList = attributeDataResponse.getColumnDataList();
                for (AttributeColumnDataResponse attributeColumnDataResponse : columnDataList) {
                    if (attributeColumnDataResponse.getColumnName().equals(name)) {
                        attributeColumnDataResponse.setValue(value);
                    }
                }
            }
        }
    }
}

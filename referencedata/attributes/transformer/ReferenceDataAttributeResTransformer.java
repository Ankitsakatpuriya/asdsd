package com.ing.bankguarantees.remote.rest.referencedata.attributes.transformer;

import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.referencedata.attributes.model.response.ReferenceDataAttributeOutput;
import com.ing.bankguarantees.remote.rest.referencedata.attributes.model.response.ReferenceDataAttributeResponse;
import com.ing.bankguarantees.remote.rest.referencedata.attributes.model.response.ReferenceDataAttributeResponse.AttributeColumnDataResponse;
import com.ing.bankguarantees.remote.rest.referencedata.attributes.model.response.ReferenceDataAttributeResponse.AttributeDataResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ReferenceDataAttributeResTransformer implements Transformer<ReferenceDataAttributeResponse, ReferenceDataAttributeOutput> {


    private final static String FEC_RISK_COLUMN = "fec_rsk_tp_code";
    private final static String FEC_RISK_VALUE = "ULTRA_HIGH";
    private final static String INDEPENDENT_INDICATOR_COLUMN = "indpndnt_ind";
    private final static String INDEPENDENT_INDICATOR_VALUE = "Y";

    @Override
    public ReferenceDataAttributeOutput transform(ReferenceDataAttributeResponse response) {
        log.info("ReferenceDataAttributeResTransformer [transform]  call");
        return ReferenceDataAttributeOutput.builder()
                .businessKeyList(response.getData()
                        .stream()
                        .filter(this::filterAttributes)
                        .map(AttributeDataResponse::getBusinessKey)
                        .toList())
                .build();

    }

    private boolean filterAttributes(AttributeDataResponse attributeData) {

        if (ObjectUtils.isNotEmpty(attributeData.getEndDate()))
            return false;

        for (AttributeColumnDataResponse columnData : attributeData.getColumnDataList()) {
            String columnName = columnData.getColumnName();
            if (columnName.equals(FEC_RISK_COLUMN) && StringUtils.equals(columnData.getValue(), FEC_RISK_VALUE)) {
                return false;
            }
            if (columnName.equals(INDEPENDENT_INDICATOR_COLUMN) && !StringUtils.equals(columnData.getValue(), INDEPENDENT_INDICATOR_VALUE)) {
                return false;
            }
        }
        return true;
    }
}

package com.ing.bankguarantees.remote.rest.referencedata.multilingual.transformer;

import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.referencedata.multilingual.model.response.ReferenceDataMultilingualResponse;
import com.ing.bankguarantees.remote.rest.referencedata.multilingual.model.response.ReferenceDataMultilingualResponse.ReferenceData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Converts response payload to required output
 */
@Component
@Slf4j
public class ReferenceDataMultilingualResTransformer implements Transformer<ReferenceDataMultilingualResponse, List<ReferenceData>> {


    @Override
    public List<ReferenceData> transform(ReferenceDataMultilingualResponse input) {

        log.info("ReferenceDataMultilingualResTransformer [transform ] response receive for reference data api call. ");
        return CollectionUtils.isEmpty(input.getData()) ? Collections.emptyList() : input.getData();

    }
}

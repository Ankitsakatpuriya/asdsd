package com.ing.bankguarantees.remote.rest.kfccreditoverview.transformer;


import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.model.response.CustomerArrangementOutput;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.model.response.CustomerArrangementResponse;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.model.response.CustomerArrangementResponse.SignaleticInformationResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class CustomerArrangementsRespTransformer implements Transformer<CustomerArrangementResponse, Optional<CustomerArrangementOutput>> {
    @Override
    public Optional<CustomerArrangementOutput> transform(CustomerArrangementResponse customerArrangementResponse) {
        List<SignaleticInformationResponse> signaleticInfoList = customerArrangementResponse.signaleticInformation();
        return CollectionUtils.isNotEmpty(signaleticInfoList)
                ? Optional.of(CustomerArrangementOutput.builder()
                .codeLanguage(signaleticInfoList.get(0).codeLanguage())
                .codeXy(signaleticInfoList.get(0).codeXy())
                .gridId(signaleticInfoList.get(0).gridId())
                .respCommercialManagerNo(signaleticInfoList.get(0).respCommercialManagerNo())
                .contraNotariety(signaleticInfoList.get(0).contraNotariety())
                .loanStatusManual(signaleticInfoList.get(0).loanStatusManual())
                .ssomi(signaleticInfoList.get(0).ssomi())
                .segMis(signaleticInfoList.get(0).segMis())
                .csiIdentifier(signaleticInfoList.get(0).involvedPartyIdentifier())
                .itvIdentifier(signaleticInfoList.get(0).involvedPartyItvIdentifier())
                .build())
                : Optional.empty();

    }
}

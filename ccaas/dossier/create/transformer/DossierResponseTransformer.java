package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.transformer;

import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.response.DossierDataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class DossierResponseTransformer implements Transformer<DossierDataResponse, String> {

    @Override
    public String transform( DossierDataResponse response) {

        return response.getId();
    }
}

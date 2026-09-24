package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.transformer;

import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.response.DossierDataResponse;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class DossierResponseTransformerTest {

    @Test
    void transformTest() {
        DossierDataResponse dossierDataResponse = MockHelper.getDossierDataResponse();
        var dossierResponseTransformer = new DossierResponseTransformer();
        var response = dossierResponseTransformer.transform(dossierDataResponse);
        assertThat(response).isNotNull();
    }
}

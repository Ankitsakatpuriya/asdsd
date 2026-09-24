package com.ing.bankguarantees.remote.rest.involveparty.invloveparties.transformer;


import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvPartyIndividualResTransformerTest {

    @Test
    void transformTestForOrg() {
        InvolvedPartyOnePamResponse involvedPartyOnePamResponse = MockHelper.getInvolvedPartyOnePamResponse();
        InvolvedPartyOnePamResponse.IndividualOnePamResponse actualOrgResponse = new InvPartyIndividualResTransformer().transform(involvedPartyOnePamResponse);
        assertThat(actualOrgResponse).isNotNull().isEqualTo(involvedPartyOnePamResponse.getIndividual());
    }

}

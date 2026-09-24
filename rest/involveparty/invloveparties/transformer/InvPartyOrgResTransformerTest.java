package com.ing.bankguarantees.remote.rest.involveparty.invloveparties.transformer;

import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.OrganisationOnePamResponse;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvPartyOrgResTransformerTest {


    @Test
    void transformTestForOrg() {
        InvolvedPartyOnePamResponse involvedPartyOnePamResponse = MockHelper.getInvolvedPartyOnePamResponse();
        OrganisationOnePamResponse actualOrgResponse = new InvPartyOrgResTransformer().transform(involvedPartyOnePamResponse);
        assertThat(actualOrgResponse).isNotNull().isEqualTo(involvedPartyOnePamResponse.getOrganisation());
    }


}

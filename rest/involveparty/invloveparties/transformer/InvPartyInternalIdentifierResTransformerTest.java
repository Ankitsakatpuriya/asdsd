package com.ing.bankguarantees.remote.rest.involveparty.invloveparties.transformer;


import com.ing.bankguarantees.models.Identifier;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.request.InvolvePartyRequest;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InvPartyInternalIdentifierResTransformerTest {

    @Test
    void transformTestForOrg() {
        InvolvedPartyOnePamResponse involvedPartyOnePamResponse = MockHelper.getInvolvedPartyOnePamResponse();
        InvolvePartyRequest request = MockHelper.getInvolvePartyRequest();
        List<Identifier> listIdentifier = new InvPartyInternalIdentifierResTransformer().transform(involvedPartyOnePamResponse, request);
        assertThat(listIdentifier).isNotNull().isEqualTo(MockHelper.getIdentifier());
    }

    @Test
    void transformTestForOrgFalse() {
        InvolvedPartyOnePamResponse involvedPartyOnePamResponse = MockHelper.getInvolvedPartyOnePamResponse();
        InvolvePartyRequest request = MockHelper.getInvolvePartyRequest();
        request.setIndividual(false);
        List<Identifier> listIdentifier = new InvPartyInternalIdentifierResTransformer()
                .transform(involvedPartyOnePamResponse, request);
        assertThat(listIdentifier).isNotNull();
    }


}

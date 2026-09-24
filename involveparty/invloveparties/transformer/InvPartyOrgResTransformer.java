package com.ing.bankguarantees.remote.rest.involveparty.invloveparties.transformer;


import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.OrganisationOnePamResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * Response transformer
 */
@Slf4j
@Component("invPartyOrgResTransformer")
public class InvPartyOrgResTransformer implements Transformer<InvolvedPartyOnePamResponse, OrganisationOnePamResponse> {

    @Override
    public OrganisationOnePamResponse transform( InvolvedPartyOnePamResponse involvedPartyOnePamResponse) {
        return involvedPartyOnePamResponse.getOrganisation();


    }
}

package com.ing.bankguarantees.remote.rest.involveparty.invloveparties.transformer;


import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.IndividualOnePamResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * Response transformer
 */
@Slf4j
@Component("invPartyIndividualResTransformer")
public class InvPartyIndividualResTransformer implements Transformer<InvolvedPartyOnePamResponse, IndividualOnePamResponse> {

    @Override
    public IndividualOnePamResponse transform( InvolvedPartyOnePamResponse involvedPartyOnePamResponse) {
        return involvedPartyOnePamResponse.getIndividual();
    }
}

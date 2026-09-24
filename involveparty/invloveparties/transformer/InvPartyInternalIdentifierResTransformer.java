package com.ing.bankguarantees.remote.rest.involveparty.invloveparties.transformer;


import com.ing.bankguarantees.models.Identifier;
import com.ing.bankguarantees.remote.common.InputTransformer;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.request.InvolvePartyRequest;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.IdentifierOnePamResponse;
import com.ing.bankguarantees.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * Response transformer
 */
@Slf4j
@Component("invPartyInternalIdentifierResTransformer")
public class InvPartyInternalIdentifierResTransformer implements InputTransformer<InvolvedPartyOnePamResponse, InvolvePartyRequest, List<Identifier>> {

    @Override
    public List<Identifier> transform(InvolvedPartyOnePamResponse involvedPartyOnePamResponse,
                                      InvolvePartyRequest involvePartyRequest) {

        log.info("InvPartyInternalIdentifierResTransformer [transform]  call Receive response from Involved Party API  endpoint to get internal identifiers ");

        return CommonUtils.convertToBankGuaranteeIdentifiers(getIdentifierList(involvedPartyOnePamResponse, involvePartyRequest));
    }

    private List<IdentifierOnePamResponse> getIdentifierList(InvolvedPartyOnePamResponse involvedPartyOnePamResponse,
                                                             InvolvePartyRequest involvePartyRequest) {
        return involvePartyRequest.isIndividual()
                ? involvedPartyOnePamResponse.getIndividual().getInvolvedPartyInternalIdentifiers()
                : involvedPartyOnePamResponse.getOrganisation().getInvolvedPartyInternalIdentifiers();
    }
}

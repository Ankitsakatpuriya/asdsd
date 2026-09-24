package com.ing.bankguarantees.remote.rest.gess.transformer;


import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.gess.model.response.GessSignResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.util.Base64;


@Slf4j
@Component
public class GessSignResponseTransformer implements Transformer<GessSignResponse, ByteArrayResource> {

    @Override
    public ByteArrayResource transform(GessSignResponse gessSignResponse) {
        log.info("Received response for gess sign  API  ");
        return new ByteArrayResource(Base64.getDecoder().decode(gessSignResponse.contents()));
    }


}

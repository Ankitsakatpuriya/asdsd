package com.ing.bankguarantees.remote.rest.ccaas.getdocument.transformer;

import com.ing.bankguarantees.remote.common.Transformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class DocumentResponseTransformer implements Transformer<byte[], ByteArrayResource> {
    @Override
    public ByteArrayResource transform(byte[] response) {
        log.info("DocumentResponseTransformer [transform] receive response for get document ");
        return new ByteArrayResource(response);
    }
}

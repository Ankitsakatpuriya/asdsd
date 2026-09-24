package com.ing.bankguarantees.remote.rest.gess.model.request;


import jdk.dynalink.linker.LinkerServices;
import lombok.Builder;

import java.util.List;

@Builder
public record GessSignRequest(

        GessDocRequest document,
        List<GessSignMetadataRequest> signatures
) {


    @Builder
    public record GessSignMetadataRequest(

            String certificateId,
            String pin,
            GessSignConfigRequest config
    ) {
    }


    @Builder
    public record GessDocRequest(String base64Content, String fileNameWithExtension) {
    }

    @Builder
    public record GessSignConfigRequest(

            SignPosition position,
            Integer pageNumber,
            Integer width,
            Integer height,
            Integer xPosition,
            Integer yPosition
    ) {
    }

}

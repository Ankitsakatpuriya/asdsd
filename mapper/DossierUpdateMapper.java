package com.ing.bankguarantees.mapper;

import com.ing.bankguarantees.models.domain.DossierUpdateData;
import com.ing.bankguarantees.models.request.DossierUpdatePayload;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = DossierUpdateMapper.class)
public interface DossierUpdateMapper {

    DossierUpdateMapper DOSSIER_UPDATE_MAPPER = Mappers.getMapper(DossierUpdateMapper.class);

    DossierUpdateData toDomainModel(DossierUpdatePayload dossierUpdatePayload);


}

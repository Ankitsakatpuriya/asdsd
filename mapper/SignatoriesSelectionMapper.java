package com.ing.bankguarantees.mapper;

import com.ing.bankguarantees.models.domain.GuaranteeDetailsData;
import com.ing.bankguarantees.models.domain.SignatoriesSelectionRequest;
import com.ing.bankguarantees.models.request.GuaranteeDetailsPayload;
import com.ing.bankguarantees.models.request.SignatoriesSelectionPayload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(uses = SignatoriesSelectionMapper.class)
public interface SignatoriesSelectionMapper {

    SignatoriesSelectionMapper SIGNATORIES_SELECTION_MAPPER = Mappers.getMapper(SignatoriesSelectionMapper.class);

    @Mapping(target = "guaranteeDetails", qualifiedByName = "mapGuaranteeDetail")
    SignatoriesSelectionRequest toDomainModel(SignatoriesSelectionPayload signatoriesSelectionPayload);


    @Named("mapGuaranteeDetail")
    default GuaranteeDetailsData<?> mapGeneric(GuaranteeDetailsPayload<?> guaranteeDetailsPayload) {

        return GuaranteeDetailsData.builder()
                .bgCode(guaranteeDetailsPayload.getBgCode())
                .bgLanguage(guaranteeDetailsPayload.getBgLanguage())
                .bgAmount(guaranteeDetailsPayload.getBgAmount())
                .bgCurrency(guaranteeDetailsPayload.getBgTypesAmtCurrency())
                .bankGuarantee(guaranteeDetailsPayload.getBankGuarantee())
                .build();
    }
}

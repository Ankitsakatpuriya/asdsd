package com.ing.bankguarantees.mapper;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.GuaranteeDetailsData;
import com.ing.bankguarantees.models.request.BankGuaranteeRequestPayload;
import com.ing.bankguarantees.models.request.GuaranteeDetailsPayload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(uses = BankGuaranteeRequestDataMapper.class)
public interface BankGuaranteeRequestDataMapper {

    BankGuaranteeRequestDataMapper BANK_GUARANTEE_REQUEST_DATA_MAPPER = Mappers.getMapper(BankGuaranteeRequestDataMapper.class);

    @Mapping(target = "guaranteeDetails", qualifiedByName = "mapGuaranteeDetail")
    BankGuaranteeRequestData toDomainModel(BankGuaranteeRequestPayload bankGuaranteeRequestPayload);

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

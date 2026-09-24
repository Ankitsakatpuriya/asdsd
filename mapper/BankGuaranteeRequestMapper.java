package com.ing.bankguarantees.mapper;

import com.ing.bankguarantees.database.entity.BankGuaranteeRequestEntity;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface BankGuaranteeRequestMapper {

    BankGuaranteeRequestMapper BANK_GUARANTEE_REQUEST_MAPPER = Mappers.getMapper(BankGuaranteeRequestMapper.class);

    BankGuaranteeRequestEntity toEntity(BankGuaranteeRequest bankGuaranteeRequest);

    BankGuaranteeRequest toDomainModel(BankGuaranteeRequestEntity bankGuaranteeRequestEntity);
    List<BankGuaranteeRequest> toDomainModelList(List<BankGuaranteeRequestEntity> bankGuaranteeRequestEntities);
}

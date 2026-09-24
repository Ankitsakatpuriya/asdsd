package com.ing.bankguarantees.mapper;

import com.ing.bankguarantees.database.entity.ReportingEntity;
import com.ing.bankguarantees.models.domain.Reporting;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReportingMapper {

    ReportingMapper REPORTING_MAPPER = Mappers.getMapper(ReportingMapper.class);

    ReportingEntity toEntity(Reporting reporting);

    Reporting toDomainModelList(ReportingEntity reportingEntity);
}

package com.ing.bankguarantees.mapper;

import com.ing.bankguarantees.database.entity.CustomDocumentEntity;
import com.ing.bankguarantees.models.domain.CustomDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CustomDocumentMapper {

    CustomDocumentMapper CUSTOM_DOCUMENT_MAPPER = Mappers.getMapper(CustomDocumentMapper.class);

    CustomDocumentEntity toEntity(CustomDocument customDocument);

    CustomDocument toDomainModel(CustomDocumentEntity customDocumentEntity);

}

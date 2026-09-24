package com.ing.bankguarantees.mapper;

import com.ing.bankguarantees.database.entity.DocumentEntity;
import com.ing.bankguarantees.models.domain.Document;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DocumentMapper {

    DocumentMapper DOCUMENT_MAPPER = Mappers.getMapper(DocumentMapper.class);

    DocumentEntity toEntity(Document document);

    Document toDomainModel(DocumentEntity documentEntity);

    List<Document> toDomainModelList(List<DocumentEntity> documentEntities);
}

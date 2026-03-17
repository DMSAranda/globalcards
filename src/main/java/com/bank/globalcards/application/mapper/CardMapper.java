package com.bank.globalcards.application.mapper;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.infrastructure.persistence.entity.CardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    CardDto toDto(Card card);

    Card toDomain(CardDto cardDto);

    @Mapping(target = "fileName", source = "fileName")
    @Mapping(target = "batchId", source = "batchId")
    @Mapping(target = "partitionNumber", source = "partitionNumber")
    @Mapping(target = "createdAt", ignore = true)
    CardEntity toEntity(Card card, String fileName, String batchId, Integer partitionNumber);

    Card toDomain(CardEntity entity);
}
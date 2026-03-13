package com.bank.globalcards.application.mapper;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.infrastructure.persistence.entity.CardEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {

    CardDto toDto(Card card);

    Card toDomain(CardDto cardDto);

    CardEntity toEntity(Card card);

    Card toDomain(CardEntity entity);

}
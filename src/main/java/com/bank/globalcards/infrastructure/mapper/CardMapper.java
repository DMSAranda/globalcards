package com.bank.globalcards.infrastructure.mapper;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.domain.enums.CardStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "status", source = "card", qualifiedByName = "mapStatus")
    CardDto toDto(Card card);

    @Mapping(target = "status", source = "status", qualifiedByName = "mapStringToStatus")
    Card toEntity(CardDto cardDto);

    @Named("mapStatus")
    default String mapStatus(Card card) {
        return card.getStatus() != null ? card.getStatus().name() : null;
    }

    @Named("mapStringToStatus")
    default CardStatus mapStringToStatus(String status) {
        return status != null ? CardStatus.valueOf(status.toUpperCase()) : CardStatus.PENDING;
    }
}

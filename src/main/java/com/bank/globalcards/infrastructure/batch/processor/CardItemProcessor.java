package com.bank.globalcards.infrastructure.batch.processor;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.infrastructure.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardItemProcessor implements ItemProcessor<Card, CardDto> {

    private final CardMapper cardMapper;

    @Override
    public CardDto process(Card card) throws Exception {
        log.debug("Transforming card to DTO: {}", card.getCardId());
        return cardMapper.toDto(card);
    }
}

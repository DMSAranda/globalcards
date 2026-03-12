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
public class CardItemProcessor implements ItemProcessor<CardDto, Card> {

    private final CardMapper cardMapper;

    @Override
    public Card process(CardDto card) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Transforming card to DTO: {}", card.getCardId());
        }
        return cardMapper.toDomain(card);
    }
}

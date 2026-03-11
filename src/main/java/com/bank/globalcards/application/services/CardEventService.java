package com.bank.globalcards.application.services;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.application.ports.out.CardEventPublisher;
import com.bank.globalcards.domain.enums.CardStatus;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.domain.records.CardEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardEventService {

    private final CardEventPublisher cardEventPublisher;

    public void publishOk(List<CardDto> cards, String batchId, String fileName, int partNumber) {

        for (CardDto card : cards) {

            CardEvent event = new CardEvent(
                    card.getCardId(),
                    card.getPan(),
                    card.getHolder(),
                    CardStatus.PROCESSED,
                    "GLOBALCARDS",
                    batchId,
                    fileName,
                    partNumber,
                    Instant.now()
            );

            cardEventPublisher.publishCardOk(event);
        }
    }

    public void publishKo(List<CardDto> cards, String batchId, String fileName, int partNumber) {

        for (CardDto card : cards) {

            CardEvent event = new CardEvent(
                    card.getCardId(),
                    card.getPan(),
                    card.getHolder(),
                    CardStatus.ERROR,
                    "GLOBALCARDS",
                    batchId,
                    fileName,
                    partNumber,
                    Instant.now()
            );

            cardEventPublisher.publishCardKo(event);
        }
    }
}
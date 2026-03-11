package com.bank.globalcards.application.services;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.application.ports.out.CardEventPublisher;
import com.bank.globalcards.domain.enums.CardStatus;
import com.bank.globalcards.domain.records.CardEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardEventService {

    private static final String SOURCE = "GLOBALCARDS";

    private final CardEventPublisher cardEventPublisher;

    public void publishOk(List<CardDto> cards, String batchId, String fileName, int partNumber) {

        cards.stream()
                .map(card -> buildEvent(card, CardStatus.PROCESSED, batchId, fileName, partNumber))
                .forEach(cardEventPublisher::publishCardOk);
    }

    public void publishKo(List<CardDto> cards, String batchId, String fileName, int partNumber) {

        cards.stream()
                .map(card -> buildEvent(card, CardStatus.ERROR, batchId, fileName, partNumber))
                .forEach(cardEventPublisher::publishCardKo);
    }

    public void publishDlq(CardDto card, String batchId, String fileName, int partNumber, String errorType, String message) {

        CardEvent event = new CardEvent(
                card.getCardId(),
                card.getPan(),
                card.getHolder(),
                CardStatus.ERROR,
                SOURCE,
                batchId,
                fileName,
                partNumber,
                Instant.now()
        );

        cardEventPublisher.publishCardDlq(event);
    }

    private CardEvent buildEvent(CardDto card,
                                 CardStatus status,
                                 String batchId,
                                 String fileName,
                                 int partNumber) {

        return new CardEvent(
                card.getCardId(),
                card.getPan(),
                card.getHolder(),
                status,
                SOURCE,
                batchId,
                fileName,
                partNumber,
                Instant.now()
        );
    }
}
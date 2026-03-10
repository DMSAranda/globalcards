package com.bank.globalcards.infrastructure.batch.writer;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.application.services.CardEventService;
import com.bank.globalcards.application.services.CardStorageService;
import com.bank.globalcards.application.services.CardValidationService;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.infrastructure.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class CardItemWriter implements ItemWriter<CardDto> {

    private final CardValidationService validationService;
    private final CardStorageService storageService;
    private final CardEventService eventService;
    private final CardMapper cardMapper;

    private final String fileName;
    private final String batchId;
    private final Integer partitionIndex;

    @Override
    public void write(Chunk<? extends CardDto> chunk) {

        if (chunk == null || chunk.isEmpty()) {
            return;
        }

        int part = partitionIndex != null ? partitionIndex : 0;

        List<Card> cards = chunk.getItems()
                .stream()
                .map(cardMapper::toEntity)
                .collect(Collectors.toList());

        log.debug("Processing {} cards for file {} partition {}", cards.size(), fileName, part);

        List<Card> validCards = cards.stream()
                .filter(validationService::isValid)
                .toList();

        List<Card> invalidCards = cards.stream()
                .filter(card -> !validationService.isValid(card))
                .toList();

        if (!validCards.isEmpty()) {
            storageService.storeChunk(validCards, fileName, part);
            eventService.publishProcessed(validCards, batchId, fileName, part);
        }

        if (!invalidCards.isEmpty()) {
            eventService.publishError(invalidCards, batchId, fileName, part);
        }

        log.debug("Chunk processed: {} valid cards, {} invalid cards",
                validCards.size(), invalidCards.size());
    }
}
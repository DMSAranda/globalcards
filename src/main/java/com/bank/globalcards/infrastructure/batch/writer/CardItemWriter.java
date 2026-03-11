package com.bank.globalcards.infrastructure.batch.writer;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.application.services.BatchMetricsService;
import com.bank.globalcards.application.services.CardEventService;
import com.bank.globalcards.application.services.CardStorageService;
import com.bank.globalcards.application.services.CardValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class CardItemWriter implements ItemWriter<CardDto> {

    private final CardValidationService validationService;
    private final CardStorageService storageService;
    private final CardEventService eventService;
    private final BatchMetricsService metricsService;

    private final String fileName;
    private final String batchId;
    private final Integer partitionIndex;

    @Override
    public void write(Chunk<? extends CardDto> chunk) {

        if (chunk == null || chunk.isEmpty()) {
            return;
        }

        int part = partitionIndex != null ? partitionIndex : 0;

        var timer = metricsService.startChunkTimer();

        List<? extends CardDto> cards = chunk.getItems();

        log.debug("Processing {} cards for file {} partition {}",
                cards.size(), fileName, part);

        Map<Boolean, List<CardDto>> partitionedCards =
                cards.stream()
                        .collect(Collectors.partitioningBy(validationService::isValid));

        List<CardDto> validCards = partitionedCards.get(true);
        List<CardDto> invalidCards = partitionedCards.get(false);

        if (!validCards.isEmpty()) {
            storageService.storeChunk(validCards, fileName, part);
            eventService.publishOk(validCards, batchId, fileName, part);
        }

        if (!invalidCards.isEmpty()) {
            eventService.publishKo(invalidCards, batchId, fileName, part);
        }

        metricsService.incrementProcessed(cards.size(), part);
        metricsService.incrementValid(validCards.size(), part);
        metricsService.incrementInvalid(invalidCards.size(), part);

        metricsService.stopChunkTimer(timer, part);

        log.debug("Chunk processed: {} valid cards, {} invalid cards",
                validCards.size(), invalidCards.size());
    }
}
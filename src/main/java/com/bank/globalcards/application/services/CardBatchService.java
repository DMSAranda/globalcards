package com.bank.globalcards.application.services;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.domain.models.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardBatchService {

    private final CardValidationService validationService;
    private final CardPersistenceService persistenceService;
    private final CardStorageService storageService;
    private final CardEventService eventService;
    private final BatchMetricsService metricsService;

    public void processChunk(
            List<Card> cards,
            String fileName,
            String batchId,
            Integer partitionIndex
    ) {

        Map<Boolean, List<Card>> partitionedCards =
                cards.stream()
                        .collect(Collectors.partitioningBy(validationService::isValid));

        List<Card> validCards = partitionedCards.get(true);
        List<Card> invalidCards = partitionedCards.get(false);

        if (!validCards.isEmpty()) {

            persistenceService.saveAll(validCards);

            storageService.storeChunk(validCards, fileName, partitionIndex);

            eventService.publishOk(validCards, batchId, fileName, partitionIndex);

            metricsService.incrementValid(validCards.size(), partitionIndex);
        }

        if (!invalidCards.isEmpty()) {

            eventService.publishKo(invalidCards, batchId, fileName, partitionIndex);

            metricsService.incrementInvalid(invalidCards.size(), partitionIndex);
        }
    }
}

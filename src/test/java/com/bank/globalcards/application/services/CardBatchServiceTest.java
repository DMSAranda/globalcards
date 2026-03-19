package com.bank.globalcards.application.services;

import com.bank.globalcards.domain.models.Card;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardBatchServiceTest {

    @Mock
    private CardValidationService validationService;

    @Mock
    private CardStorageService storageService;

    @Mock
    private CardEventService eventService;

    @Mock
    private BatchMetricsService metricsService;

    @InjectMocks
    private CardBatchService service;

    @Test
    void shouldProcessValidCards() {

        Card card = Card.builder()
                .cardId("136588")
                .pan("958775588")
                .holder("69855477")
                .build();

        when(validationService.isValid(card)).thenReturn(true);

        service.processChunk(
                List.of(card),
                "part1.csv",
                "1",
                1
        );

        verify(storageService).storeChunk(any(), eq("part1.csv"), eq(1));
        verify(eventService).publishOk(any(), eq("1"), eq("part1.csv"), eq(1));
        verify(metricsService).incrementValid(1,1);
    }

    @Test
    void shouldSendInvalidCardsToErrorTopic() {

        Card card = Card.builder().cardId("1").build();

        when(validationService.isValid(card)).thenReturn(false);

        service.processChunk(
                List.of(card),
                "part1.csv",
                "1",
                1
        );

        verify(eventService).publishKo(any(), eq("1"), eq("part1.csv"), eq(1));
        verify(metricsService).incrementInvalid(1,1);
    }
}
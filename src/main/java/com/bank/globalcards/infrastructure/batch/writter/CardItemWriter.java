package com.bank.globalcards.infrastructure.batch.writter;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.application.services.CardProcessingService;
import com.bank.globalcards.infrastructure.mapper.CardMapper;
import com.bank.globalcards.domain.models.Card;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class CardItemWriter implements ItemWriter<CardDto> {

    private final CardProcessingService cardProcessingService;
    private final CardMapper cardMapper;
    private final String fileName;
    private final String batchId;
    private Integer partitionIndex; // puede inyectarse o rellenarse en beforeStep

    public CardItemWriter(CardProcessingService cardProcessingService, CardMapper cardMapper, String fileName, String batchId, Integer partitionIndex) {
        this.cardProcessingService = Objects.requireNonNull(cardProcessingService);
        this.cardMapper = Objects.requireNonNull(cardMapper);
        this.fileName = fileName;
        this.batchId = batchId;
        this.partitionIndex = partitionIndex;
    }

    public CardItemWriter(CardProcessingService cardProcessingService, CardMapper cardMapper, String fileName, String batchId) {
        this(cardProcessingService, cardMapper, fileName, batchId, null);
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        try {
            if (this.partitionIndex == null) {
                Object idx = stepExecution.getExecutionContext().get("partitionIndex");
                if (idx instanceof Integer) {
                    this.partitionIndex = (Integer) idx;
                } else if (idx instanceof String) {
                    this.partitionIndex = Integer.parseInt((String) idx);
                }
            }
        } catch (Exception e) {
            log.warn("Unable to read partitionIndex from StepExecution executionContext: {}", e.getMessage());
            if (this.partitionIndex == null) this.partitionIndex = 0;
        }
    }

    @Override
    public void write(List<? extends CardDto> items) throws Exception {
        if (items == null || items.isEmpty()) {
            log.debug("No items to write for file {} partition {}", fileName, partitionIndex);
            return;
        }

        List<Card> cards = items.stream()
                .map(cardMapper::toEntity)
                .collect(Collectors.toList());

        // Construir CardFile mínimo para pasar metadata
        var cardFile = com.bank.globalcards.domain.models.CardFile.builder()
                .fileName(this.fileName)
                .batchId(this.batchId)
                .multipart(true)
                .uploadTimestamp(java.time.LocalDateTime.now())
                .build();

        int part = (this.partitionIndex != null) ? this.partitionIndex : 0;

        log.debug("Writing {} cards for file {} partition {}", cards.size(), fileName, part);

        // Delegar lógica de persistencia/upload al servicio de aplicación
        cardProcessingService.processCardBatch(cards, cardFile, part);
    }
}

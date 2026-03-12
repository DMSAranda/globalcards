package com.bank.globalcards.infrastructure.batch.writer;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.application.services.CardBatchService;
import com.bank.globalcards.domain.models.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CardItemWriter implements ItemWriter<Card> {

    private final CardBatchService cardBatchService;

    private final String fileName;
    private final String batchId;
    private final Integer partitionIndex;

    @Override
    public void write(Chunk<? extends Card> chunk) {

        if (chunk.isEmpty()) return;

        List<Card> items = new ArrayList<>(chunk.getItems());

        cardBatchService.processChunk(
                items,
                fileName,
                batchId,
                partitionIndex
        );
    }
}
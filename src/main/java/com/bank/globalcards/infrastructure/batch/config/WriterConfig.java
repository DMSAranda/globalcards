package com.bank.globalcards.infrastructure.batch.config;

import com.bank.globalcards.application.services.*;
import com.bank.globalcards.infrastructure.batch.writer.CardItemWriter;
import com.bank.globalcards.infrastructure.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@RequiredArgsConstructor
public class WriterConfig {

    private final CardBatchService cardBatchService;

    @Bean
    @StepScope
    public CardItemWriter cardItemWriter(
            @Value("#{jobParameters['fileName']}") String fileName,
            @Value("#{jobParameters['batchId']}") String batchId,
            @Value("#{stepExecutionContext['partitionIndex']}") Integer partitionIndex) {

        return new CardItemWriter(
                cardBatchService,
                fileName,
                batchId,
                partitionIndex
        );
    }
}
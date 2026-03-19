package com.bank.globalcards.infrastructure.batch.step;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.infrastructure.batch.listener.BatchSkipListener;
import com.bank.globalcards.infrastructure.batch.listener.BatchStepExecutionListener;
import com.bank.globalcards.infrastructure.batch.config.DynamicBatchConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.repository.JobRepository;

@Configuration
@RequiredArgsConstructor
public class WorkerStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DynamicBatchConfig dynamicBatchConfig;

    private final ItemReader<CardDto> reader;
    private final ItemProcessor<CardDto, Card> processor;
    private final ItemWriter<Card> writer;

    private final BatchStepExecutionListener batchStepExecutionListener;
    private final BatchSkipListener batchSkipListener;

    @Bean
    public Step workerStep() {

        return new StepBuilder("workerStep", jobRepository)
                .<CardDto, Card>chunk(dynamicBatchConfig.getChunkSize(), transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(dynamicBatchConfig.getMaxRetries())
                .skip(Exception.class)
                .skipLimit(dynamicBatchConfig.getSkipLimit())
                .listener(batchStepExecutionListener)
                .listener(batchSkipListener)
                .build();
    }
}

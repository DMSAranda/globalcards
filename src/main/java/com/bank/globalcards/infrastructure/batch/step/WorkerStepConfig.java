package com.bank.globalcards.infrastructure.batch.step;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.infrastructure.batch.listener.BatchStepExecutionListener;
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

    private final ItemReader<Card> reader;
    private final ItemProcessor<Card, CardDto> processor;
    private final ItemWriter<CardDto> writer;
    private final BatchStepExecutionListener batchStepExecutionListener;

    @Bean
    public Step workerStep() {

        return new StepBuilder("workerStep", jobRepository)
                .<Card, CardDto>chunk(1000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(3)
                .skip(Exception.class)
                .skipLimit(50)
                .listener(batchStepExecutionListener)
                .build();
    }
}
package com.bank.globalcards.infrastructure.batch.job;

import com.bank.globalcards.application.services.CardProcessingService;
import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.infrastructure.batch.reader.S3CardItemReader;
import com.bank.globalcards.infrastructure.batch.partitioner.S3FilePartitioner;
import com.bank.globalcards.infrastructure.batch.processor.CardItemProcessor;
import com.bank.globalcards.infrastructure.batch.writter.CardItemWriter;
import com.bank.globalcards.infrastructure.mapper.CardMapper;
import com.bank.globalcards.infrastructure.s3.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class CardProcessingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CardProcessingService cardProcessingService;
    private final S3Properties s3Properties;
    private final S3Client s3Client;
    private final CardMapper cardMapper;

    @Bean
    public Job cardProcessingJob(Step masterStep) {
        return new JobBuilder("cardProcessingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(masterStep)
                .build();
    }

    @Bean
    public Step masterStep(S3FilePartitioner partitioner,
                           TaskExecutorPartitionHandler partitionHandler) {

        return new StepBuilder("masterStep", jobRepository)
                .partitioner("cardProcessingStep", partitioner)
                .partitionHandler(partitionHandler)
                .build();
    }

    @Bean
    public Step cardProcessingStep(S3CardItemReader reader,
                                   CardItemProcessor processor,
                                   CardItemWriter writer) {

        return new StepBuilder("cardProcessingStep", jobRepository)
                .<Card, CardDto>chunk(1000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public TaskExecutorPartitionHandler partitionHandler(Step cardProcessingStep) {

        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setTaskExecutor(taskExecutor());
        handler.setStep(cardProcessingStep);
        handler.setGridSize(4); // Número de particiones paralelas

        return handler;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("partition-");
        executor.initialize();
        return executor;
    }

    @Bean
    @JobScope
    public S3FilePartitioner s3FilePartitioner(
            @Value("#{jobParameters['fileName']}") String fileName) {

        return new S3FilePartitioner(
                s3Client,
                s3Properties,
                fileName != null ? fileName : "cards.csv",
                4
        );
    }

    @Bean
    @StepScope
    public S3CardItemReader s3CardItemReader(
            @Value("#{jobParameters['fileName']}") String fileName,
            @Value("#{stepExecutionContext['start']}") Integer start,
            @Value("#{stepExecutionContext['end']}") Integer end) {

        return new S3CardItemReader(
                s3Client,
                s3Properties,
                fileName,
                start,
                end
        );
    }

    @Bean
    public CardItemProcessor cardItemProcessor() {
        return new CardItemProcessor(cardMapper);
    }

    @Bean
    @JobScope
    public CardItemWriter cardItemWriter(
            @Value("#{jobParameters['fileName']}") String fileName,
            @Value("#{jobParameters['batchId']}") String batchId) {

        return new CardItemWriter(
                cardProcessingService,
                cardMapper,
                fileName != null ? fileName : "cards.csv",
                batchId != null ? batchId : java.util.UUID.randomUUID().toString()
        );
    }
}
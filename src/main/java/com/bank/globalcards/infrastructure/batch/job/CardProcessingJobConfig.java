package com.bank.globalcards.infrastructure.batch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CardProcessingJobConfig {

    private final JobRepository jobRepository;
    private final Step masterStep;

    @Bean
    public Job cardProcessingJob() {

        return new JobBuilder("cardProcessingJob", jobRepository)
                .start(masterStep)
                .build();
    }
}
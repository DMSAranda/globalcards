package com.bank.globalcards.infrastructure.batch.step;

import com.bank.globalcards.infrastructure.batch.partition.S3FilePartitioner;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class MasterStepConfig {

    private static final int GRID_SIZE = 10;

    private final JobRepository jobRepository;

    private final Step workerStep;
    private final S3FilePartitioner partitioner;
    private final TaskExecutor taskExecutor;

    @Bean
    public Step masterStep() {

        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();

        partitionHandler.setTaskExecutor(taskExecutor);
        partitionHandler.setStep(workerStep);
        partitionHandler.setGridSize(GRID_SIZE);

        return new StepBuilder("masterStep", jobRepository)
                .partitioner(workerStep.getName(), partitioner)
                .partitionHandler(partitionHandler)
                .build();
    }
}
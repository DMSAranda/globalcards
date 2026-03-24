package com.bank.globalcards.infrastructure.batch.listener;

import com.bank.globalcards.application.services.BatchJobService;
import com.bank.globalcards.infrastructure.persistence.entity.BatchJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

import com.bank.globalcards.application.services.BatchMetricsService;

@Component
@RequiredArgsConstructor
public class BatchJobExecutionListener implements JobExecutionListener {

    private final BatchJobService batchJobService;
    private final BatchMetricsService metricsService;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String batchId = jobExecution.getJobParameters().getString("batchId", jobExecution.getJobId().toString());
        String fileName = jobExecution.getJobParameters().getString("fileName", jobExecution.getJobInstance().getJobName());

        BatchJob batchJob = BatchJob.builder()
                .batchId(batchId)
                .fileName(fileName)
                .status("RUNNING")
                .startTime(Instant.now())
                .build();

        batchJobService.create(batchJob);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String batchId = jobExecution.getJobParameters().getString("batchId", jobExecution.getJobId().toString());
        Optional<BatchJob> job = batchJobService.findByBatchId(batchId);

        if (job.isPresent()) {
            BatchJob batchJob = job.get();
            Instant endTime = Instant.now();
            Instant startTime = batchJob.getStartTime() != null ? batchJob.getStartTime() : endTime;

            batchJob.setTotalRecords(jobExecution.getStepExecutions().stream()
                    .mapToLong(StepExecution::getReadCount)
                    .sum());
            batchJob.setProcessedRecords(jobExecution.getStepExecutions().stream()
                    .mapToLong(StepExecution::getWriteCount)
                    .sum());
            batchJob.setFailedRecords(jobExecution.getStepExecutions().stream()
                    .mapToLong(StepExecution::getSkipCount)
                    .sum());

            if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                batchJob.markAsCompleted();
                metricsService.recordBatchCompletion(
                        batchId,
                        java.time.Duration.between(startTime, endTime),
                        1,
                        Math.toIntExact(batchJob.getTotalRecords() != null ? batchJob.getTotalRecords() : 0L)
                );
            } else {
                batchJob.markAsFailed("Job failed");
            }
            batchJobService.update(batchJob);
        }
    }
}
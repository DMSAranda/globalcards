package com.bank.globalcards.infrastructure.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.BatchStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchJobExecutionListener implements JobExecutionListener {

    private long startTime;

    @Override
    public void beforeJob(JobExecution jobExecution) {

        startTime = System.currentTimeMillis();

        log.info("======================================");
        log.info("BATCH JOB STARTED");
        log.info("JobName: {}", jobExecution.getJobInstance().getJobName());
        log.info("JobId: {}", jobExecution.getJobId());
        log.info("======================================");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        long duration = System.currentTimeMillis() - startTime;

        log.info("======================================");
        log.info("BATCH JOB FINISHED");
        log.info("Status: {}", jobExecution.getStatus());
        log.info("Duration: {} ms", duration);
        log.info("======================================");

        if (jobExecution.getStatus() == BatchStatus.FAILED) {
            jobExecution.getAllFailureExceptions()
                    .forEach(ex -> log.error("Batch error", ex));
        }
    }
}
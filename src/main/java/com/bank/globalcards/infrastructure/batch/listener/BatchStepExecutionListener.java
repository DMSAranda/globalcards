package com.bank.globalcards.infrastructure.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchStepExecutionListener implements StepExecutionListener {

    private long startTime;

    @Override
    public void beforeStep(StepExecution stepExecution) {

        startTime = System.currentTimeMillis();

        log.info("STEP STARTED: {}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        long duration = System.currentTimeMillis() - startTime;

        log.info("STEP FINISHED: {}", stepExecution.getStepName());
        log.info("ReadCount: {}", stepExecution.getReadCount());
        log.info("WriteCount: {}", stepExecution.getWriteCount());
        log.info("SkipCount: {}", stepExecution.getSkipCount());
        log.info("Duration: {} ms", duration);

        return stepExecution.getExitStatus();
    }
}
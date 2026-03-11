package com.bank.globalcards.infrastructure.batch.listener;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.application.services.CardEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchSkipListener implements SkipListener<CardDto, CardDto> {

    private final CardEventService eventService;

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("Error reading item in batch", t);
    }

    @Override
    public void onSkipInProcess(CardDto item, Throwable t) {

        log.error("Error processing card {}", item, t);

        StepExecution stepExecution =
                StepSynchronizationManager.getContext().getStepExecution();

        ExecutionContext context = stepExecution.getExecutionContext();

        String fileName = context.getString("fileName");
        int partitionIndex = context.getInt("partitionIndex");

        String batchId =
                stepExecution.getJobParameters().getString("batchId");

        eventService.publishError(
                item,
                batchId,
                fileName,
                partitionIndex,
                "PROCESS_ERROR",
                t.getMessage()
        );
    }

    @Override
    public void onSkipInWrite(CardDto item, Throwable t) {

        log.error("Error writing card {}", item, t);

        StepExecution stepExecution =
                StepSynchronizationManager.getContext().getStepExecution();

        ExecutionContext context = stepExecution.getExecutionContext();

        String fileName = context.getString("fileName");
        int partitionIndex = context.getInt("partitionIndex");

        String batchId =
                stepExecution.getJobParameters().getString("batchId");

        eventService.publishError(
                item,
                batchId,
                fileName,
                partitionIndex,
                "WRITE_ERROR",
                t.getMessage()
        );
    }
}
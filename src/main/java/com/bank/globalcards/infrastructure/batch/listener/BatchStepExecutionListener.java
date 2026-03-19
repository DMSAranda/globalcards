package com.bank.globalcards.infrastructure.batch.listener;

import com.bank.globalcards.application.services.BatchJobService;
import com.bank.globalcards.application.services.CardBatchService;
import com.bank.globalcards.application.services.BatchMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchStepExecutionListener implements StepExecutionListener {

    private final BatchMetricsService metricsService;
    private final BatchJobService batchJobService;
    private final CardBatchService cardBatchService;

    private Instant startTime;
    private String fileName;
    private String batchId;
    private int partitionIndex;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        startTime = Instant.now();
        
        // Extraer parámetros del step execution
        fileName = stepExecution.getJobParameters().getString("fileName");
        batchId = stepExecution.getJobParameters().getString("batchId");
        partitionIndex = stepExecution.getStepName().contains("partition") ? 
                Integer.parseInt(stepExecution.getStepName().split("-")[1]) : 0;

        // Iniciar métricas de partición
        metricsService.recordPartitionStart(fileName, partitionIndex);

        log.info("STEP STARTED: {} for file: {}, batch: {}, partition: {}", 
                stepExecution.getStepName(), fileName, batchId, partitionIndex);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        Duration duration = Duration.between(startTime, Instant.now());

        // Registrar métricas de partición completada
        metricsService.recordPartitionComplete(
                fileName, 
                partitionIndex, 
                duration,
                (int) stepExecution.getWriteCount(), // Convertir long a int
                (int) stepExecution.getSkipCount()   // Convertir long a int
        );

        // Marcar checkpoint como completado
        try {
            cardBatchService.completePartition(batchId, fileName, partitionIndex);
        } catch (Exception e) {
            log.error("Error marking partition as completed: {}", e.getMessage(), e);
        }

        // Logging enriquecido (manteniendo el formato original pero con más info)
        log.info("STEP FINISHED: {}", stepExecution.getStepName());
        log.info("File: {}, Batch: {}, Partition: {}", fileName, batchId, partitionIndex);
        log.info("ReadCount: {}", stepExecution.getReadCount());
        log.info("WriteCount: {}", stepExecution.getWriteCount());
        log.info("SkipCount: {}", stepExecution.getSkipCount());
        log.info("CommitCount: {}", stepExecution.getCommitCount());
        log.info("RollbackCount: {}", stepExecution.getRollbackCount());
        log.info("Duration: {} ms ({} seconds)", duration.toMillis(), duration.toSeconds());

        // Calcular throughput
        if (duration.toMillis() > 0) {
            double throughput = (double) stepExecution.getReadCount() / (duration.toMillis() / 1000.0);
            log.info("Throughput: {:.2f} records/second", throughput);
        }

        // Verificar si el batch está completo
        try {
            if (cardBatchService.isBatchCompleted(batchId)) {
                metricsService.recordBatchCompletion(
                        batchId, 
                        duration, 
                        1, // files - esto debería calcularse mejor
                        (int) stepExecution.getReadCount()
                );
                log.info("BATCH COMPLETED: {}", batchId);
            }
        } catch (Exception e) {
            log.error("Error checking batch completion: {}", e.getMessage(), e);
        }

        return stepExecution.getExitStatus();
    }
}

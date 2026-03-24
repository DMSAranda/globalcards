package com.bank.globalcards.infrastructure.batch.listener;

import com.bank.globalcards.application.services.BatchMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import org.springframework.batch.item.ExecutionContext;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchStepExecutionListener implements StepExecutionListener {

    private final BatchMetricsService metricsService;

    private Instant startTime;
    private String fileName;
    private String batchId;
    private int partitionIndex;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        startTime = Instant.now();

        // Extraer parámetros del step execution (prioridad al contexto de partición)
        ExecutionContext context = stepExecution.getExecutionContext();
        fileName = context.containsKey("fileName")
                ? context.getString("fileName")
                : stepExecution.getJobParameters().getString("fileName");
        batchId = stepExecution.getJobParameters().getString("batchId");
        partitionIndex = context.containsKey("partitionIndex")
                ? context.getInt("partitionIndex")
                : 0;

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
            log.info("Throughput: {} records/second", String.format("%.2f", throughput));
        }

        return stepExecution.getExitStatus();
    }
}

package com.bank.globalcards.application.services;

import io.micrometer.core.instrument.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchMetricsService {

    private final MeterRegistry meterRegistry;

    // Contadores básicos (manteniendo compatibilidad)
    private final AtomicLong totalProcessedRecords = new AtomicLong(0);
    private final AtomicLong totalValidRecords = new AtomicLong(0);
    private final AtomicLong totalInvalidRecords = new AtomicLong(0);
    private final AtomicLong activePartitions = new AtomicLong(0);

    // Métodos originales (manteniendo compatibilidad)
    public void incrementProcessed(int count, int partition) {
        Counter.builder("cards.processed.total")
                .tag("partition", String.valueOf(partition))
                .register(meterRegistry)
                .increment(count);
        totalProcessedRecords.addAndGet(count);
    }

    public void incrementValid(int count, int partition) {
        Counter.builder("cards.valid.total")
                .tag("partition", String.valueOf(partition))
                .register(meterRegistry)
                .increment(count);
        totalValidRecords.addAndGet(count);
    }

    public void incrementInvalid(int count, int partition) {
        Counter.builder("cards.invalid.total")
                .tag("partition", String.valueOf(partition))
                .register(meterRegistry)
                .increment(count);
        totalInvalidRecords.addAndGet(count);
    }

    public Timer.Sample startChunkTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopChunkTimer(Timer.Sample sample, int partition) {
        sample.stop(
                Timer.builder("batch.chunk.processing.time")
                        .tag("partition", String.valueOf(partition))
                        .register(meterRegistry)
        );
    }

    // Nuevos métodos mejorados del EnhancedBatchMetricsService
    public void recordPartitionStart(String fileName, int partitionId) {
        activePartitions.incrementAndGet();
        
        Counter.builder("batch.partitions.started")
                .tag("file", fileName)
                .tag("partition", String.valueOf(partitionId))
                .register(meterRegistry)
                .increment();

        log.debug("Partition {} started for file {}", partitionId, fileName);
    }

    public void recordPartitionComplete(String fileName, int partitionId, Duration processingTime, 
                                      int validRecords, int invalidRecords) {
        activePartitions.decrementAndGet();

        // Timer de partición
        Timer.builder("batch.partition.processing.time")
                .tag("file", fileName)
                .tag("partition", String.valueOf(partitionId))
                .register(meterRegistry)
                .record(processingTime);

        // Contadores de resultados
        Counter.builder("batch.partition.records.valid")
                .tag("file", fileName)
                .tag("partition", String.valueOf(partitionId))
                .register(meterRegistry)
                .increment(validRecords);

        Counter.builder("batch.partition.records.invalid")
                .tag("file", fileName)
                .tag("partition", String.valueOf(partitionId))
                .register(meterRegistry)
                .increment(invalidRecords);

        // Throughput
        if (processingTime.toMillis() > 0) {
            double throughput = (double) (validRecords + invalidRecords) / processingTime.toSeconds();
            Gauge.builder("batch.partition.throughput", this, obj -> throughput)
                    .tag("file", fileName)
                    .tag("partition", String.valueOf(partitionId))
                    .register(meterRegistry);
        }

        log.debug("Partition {} completed for file {}: valid={}, invalid={}, time={}", 
                partitionId, fileName, validRecords, invalidRecords, processingTime);
    }

    public void recordBatchCompletion(String batchId, Duration totalProcessingTime, 
                                   int totalFiles, int totalRecords) {
        Timer.builder("batch.completion.time")
                .tag("batch_id", batchId)
                .register(meterRegistry)
                .record(totalProcessingTime);

        Gauge.builder("batch.files.total", this, obj -> totalFiles)
                .tag("batch_id", batchId)
                .register(meterRegistry);

        Gauge.builder("batch.records.total", this, obj -> totalRecords)
                .tag("batch_id", batchId)
                .register(meterRegistry);

        log.info("Batch {} completed: files={}, records={}, time={}", 
                batchId, totalFiles, totalRecords, totalProcessingTime);
    }

    public void recordProcessingError(String component, String errorType) {
        Counter.builder("batch.errors")
                .tag("component", component)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();

        log.warn("Recorded error metric for component {}: {}", component, errorType);
    }

    public void recordS3Operation(String operation, Duration duration, boolean success) {
        Timer.builder("s3.operation.duration")
                .tag("operation", operation)
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
                .record(duration);

        if (!success) {
            Counter.builder("s3.operations.failed")
                    .tag("operation", operation)
                    .register(meterRegistry)
                    .increment();
        }
    }

    public void recordKafkaMessage(String topic, boolean success) {
        Counter.builder("kafka.messages.produced")
                .tag("topic", topic)
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
                .increment();
    }

    // Getters para totales
    public long getTotalProcessedRecords() {
        return totalProcessedRecords.get();
    }

    public long getTotalValidRecords() {
        return totalValidRecords.get();
    }

    public long getTotalInvalidRecords() {
        return totalInvalidRecords.get();
    }

    public long getActivePartitionsCount() {
        return activePartitions.get();
    }

    // Métricas de sistema (simplificadas)
    public void recordMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        
        Gauge.builder("jvm.memory.used", this, obj -> usedMemory)
                .tag("area", "heap")
                .register(meterRegistry);
    }
}

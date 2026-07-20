package com.bank.globalcards.application.services;

import com.bank.globalcards.application.ports.out.BatchJobRepository;
import com.bank.globalcards.infrastructure.persistence.entity.BatchJob;
import com.bank.globalcards.infrastructure.persistence.entity.BatchJobPartitionProgress;
import com.bank.globalcards.infrastructure.persistence.repository.BatchJobPartitionProgressJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final BatchJobRepository batchJobRepository;
    private final BatchJobPartitionProgressJpaRepository partitionProgressRepository;

    public BatchJob create(BatchJob batchJob) {
        log.info("Creating batch job with batchId {}", batchJob.getBatchId());
        return batchJobRepository.save(batchJob);
    }

    public Optional<BatchJob> findByBatchId(String batchId) {
        log.debug("Finding batch job by batchId {}", batchId);
        return batchJobRepository.findByBatchId(batchId);
    }

    public List<BatchJob> findByStatus(String status) {
        log.debug("Finding batch jobs with status {}", status);
        return batchJobRepository.findByStatus(status);
    }

    public List<BatchJob> findAll() {
        log.debug("Fetching all batch jobs");
        return batchJobRepository.findAll();
    }

    public BatchJob update(BatchJob batchJob) {
        log.info("Updating batch job {}", batchJob.getBatchId());
        return batchJobRepository.save(batchJob);
    }

    public void deleteByBatchId(String batchId) {
        log.warn("Deleting batch job {}", batchId);
        batchJobRepository.deleteByBatchId(batchId);
    }

    // Nuevos métodos para checkpoints usando BatchJob existente
    @Transactional
    public BatchJob saveBatchJobProgress(String batchId, String fileName, int partitionNumber,
                                       long processedBytes, int processedRecords,
                                       int validRecords, int invalidRecords, String lastCardId) {
        
        BatchJob batchJob = batchJobRepository.findByBatchIdAndFileName(batchId, fileName)
                .orElseGet(() -> BatchJob.builder()
                        .batchId(batchId)
                        .fileName(fileName)
                        .status("RUNNING")
                        .startTime(java.time.Instant.now())
                        .build());

        BatchJobPartitionProgress progress = partitionProgressRepository
                .findByBatchIdAndFileNameAndPartitionIndex(batchId, fileName, partitionNumber)
                .orElseGet(() -> BatchJobPartitionProgress.builder()
                        .batchId(batchId)
                        .fileName(fileName)
                        .partitionIndex(partitionNumber)
                        .processedBytes(0L)
                        .processedRecords(0L)
                        .validRecords(0L)
                        .invalidRecords(0L)
                        .status("RUNNING")
                        .build());

        progress.setProcessedBytes((progress.getProcessedBytes() != null ? progress.getProcessedBytes() : 0L) + processedBytes);
        progress.setProcessedRecords((progress.getProcessedRecords() != null ? progress.getProcessedRecords() : 0L) + processedRecords);
        progress.setValidRecords((progress.getValidRecords() != null ? progress.getValidRecords() : 0L) + validRecords);
        progress.setInvalidRecords((progress.getInvalidRecords() != null ? progress.getInvalidRecords() : 0L) + invalidRecords);
        if (lastCardId != null && !lastCardId.isBlank()) {
            progress.setLastCardId(lastCardId);
        }

        partitionProgressRepository.save(progress);

        // Consolidar cabecera a partir del detalle para mantener consistencia.
        List<BatchJobPartitionProgress> partitionRows =
                partitionProgressRepository.findByBatchIdAndFileNameOrderByPartitionIndexAsc(batchId, fileName);

        long totalProcessed = partitionRows.stream()
                .mapToLong(row -> row.getProcessedRecords() != null ? row.getProcessedRecords() : 0L)
                .sum();
        long totalInvalid = partitionRows.stream()
                .mapToLong(row -> row.getInvalidRecords() != null ? row.getInvalidRecords() : 0L)
                .sum();

        batchJob.setProcessedRecords(totalProcessed);
        batchJob.setTotalRecords(totalProcessed);
        batchJob.setFailedRecords(totalInvalid);

        BatchJob saved = batchJobRepository.save(batchJob);
        
        log.debug("Saved batch job progress for batch {} file {}: {} records processed", 
                batchId, fileName, processedRecords);
        
        return saved;
    }

    @Transactional
    public void markPartitionAsCompleted(String batchId, String fileName, int partitionNumber) {
        partitionProgressRepository
                .findByBatchIdAndFileNameAndPartitionIndex(batchId, fileName, partitionNumber)
                .ifPresent(progress -> {
                    progress.markAsCompleted();
                    partitionProgressRepository.save(progress);
                });
    }

    @Transactional
    public void markBatchJobAsCompleted(String batchId, String fileName) {
        batchJobRepository.findByBatchIdAndFileName(batchId, fileName)
                .ifPresent(batchJob -> {
                    batchJob.markAsCompleted();
                    batchJobRepository.save(batchJob);
                    log.info("Marked batch job as completed for batch {} file {}", batchId, fileName);
                });
    }

    @Transactional(readOnly = true)
    public boolean isBatchCompleted(String batchId) {
        List<BatchJob> batchJobs = batchJobRepository.findByBatchIdOrderByFileNameAsc(batchId);
        
        if (batchJobs.isEmpty()) {
            return false;
        }

        long completedCount = batchJobs.stream()
                .mapToLong(job -> "COMPLETED".equals(job.getStatus()) ? 1 : 0)
                .sum();
        
        return completedCount == batchJobs.size();
    }

    @Transactional(readOnly = true)
    public BatchProgress getBatchProgress(String batchId) {
        List<BatchJob> batchJobs = batchJobRepository.findByBatchIdOrderByFileNameAsc(batchId);
        
        if (batchJobs.isEmpty()) {
            return new BatchProgress(0, 0, 0, 0, false);
        }

        int totalRecords = batchJobs.stream()
                .mapToInt(job -> job.getTotalRecords() != null ? job.getTotalRecords().intValue() : 0)
                .sum();
        
        int totalValid = batchJobs.stream()
                .mapToInt(job -> {
                    int processed = job.getProcessedRecords() != null ? job.getProcessedRecords().intValue() : 0;
                    int failed = job.getFailedRecords() != null ? job.getFailedRecords().intValue() : 0;
                    return Math.max(0, processed - failed); // válidos = procesados - fallidos
                })
                .sum();
        
        int totalInvalid = batchJobs.stream()
                .mapToInt(job -> job.getFailedRecords() != null ? job.getFailedRecords().intValue() : 0)
                .sum();
        
        long completedCount = batchJobs.stream()
                .mapToLong(job -> "COMPLETED".equals(job.getStatus()) ? 1 : 0)
                .sum();
        
        boolean isCompleted = completedCount == batchJobs.size();

        return new BatchProgress(totalRecords, totalValid, totalInvalid, (int) completedCount, isCompleted);
    }

    // Clase para progreso
    public static class BatchProgress {
        private final int totalRecords;
        private final int validRecords;
        private final int invalidRecords;
        private final int completedPartitions;
        private final boolean isCompleted;

        public BatchProgress(int totalRecords, int validRecords, int invalidRecords, 
                           int completedPartitions, boolean isCompleted) {
            this.totalRecords = totalRecords;
            this.validRecords = validRecords;
            this.invalidRecords = invalidRecords;
            this.completedPartitions = completedPartitions;
            this.isCompleted = isCompleted;
        }

        // Getters
        public int getTotalRecords() { return totalRecords; }
        public int getValidRecords() { return validRecords; }
        public int getInvalidRecords() { return invalidRecords; }
        public int getCompletedPartitions() { return completedPartitions; }
        public boolean isCompleted() { return isCompleted; }
    }
}

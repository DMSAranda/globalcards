package com.bank.globalcards.application.services;

import com.bank.globalcards.application.ports.out.BatchJobRepository;
import com.bank.globalcards.infrastructure.persistence.entity.BatchJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final BatchJobRepository batchJobRepository;

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
}
package com.bank.globalcards.application.ports.out;

import com.bank.globalcards.infrastructure.persistence.entity.BatchJob;

import java.util.List;
import java.util.Optional;

public interface BatchJobRepository {

    BatchJob save(BatchJob batchJob);
    
    Optional<BatchJob> findByBatchId(String batchId);
    
    Optional<BatchJob> findByBatchIdAndFileName(String batchId, String fileName);
    
    List<BatchJob> findByBatchIdOrderByFileNameAsc(String batchId);
    
    List<BatchJob> findByStatus(String status);
    
    List<BatchJob> findAll();
    
    void deleteByBatchId(String batchId);
}

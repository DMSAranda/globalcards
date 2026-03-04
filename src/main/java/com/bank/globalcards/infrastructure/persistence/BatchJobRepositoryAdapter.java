package com.bank.globalcards.infrastructure.persistence;

import com.bank.globalcards.application.ports.out.BatchJobRepository;
import com.bank.globalcards.domain.models.BatchJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BatchJobRepositoryAdapter implements BatchJobRepository {

    private final BatchJobJpaRepository jpaRepository;

    @Override
    public BatchJob save(BatchJob batchJob) {
        return jpaRepository.save(batchJob);
    }

    @Override
    public Optional<BatchJob> findByBatchId(String batchId) {
        return jpaRepository.findByBatchId(batchId);
    }

    @Override
    public List<BatchJob> findByStatus(String status) {
        return jpaRepository.findByStatus(status);
    }

    @Override
    public List<BatchJob> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteByBatchId(String batchId) {
        jpaRepository.deleteByBatchId(batchId);
    }
}

package com.bank.globalcards.infrastructure.persistence.repository;

import com.bank.globalcards.infrastructure.persistence.entity.BatchJobPartitionProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchJobPartitionProgressJpaRepository extends JpaRepository<BatchJobPartitionProgress, Long> {

    Optional<BatchJobPartitionProgress> findByBatchIdAndFileNameAndPartitionIndex(
            String batchId,
            String fileName,
            Integer partitionIndex
    );

    List<BatchJobPartitionProgress> findByBatchIdAndFileNameOrderByPartitionIndexAsc(String batchId, String fileName);
}

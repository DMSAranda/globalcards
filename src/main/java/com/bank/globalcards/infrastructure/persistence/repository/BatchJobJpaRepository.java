package com.bank.globalcards.infrastructure.persistence.repository;

import com.bank.globalcards.infrastructure.persistence.entity.BatchJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchJobJpaRepository extends JpaRepository<BatchJob, Long> {

    Optional<BatchJob> findByBatchId(String batchId);

    List<BatchJob> findByStatus(String status);

    @Query("SELECT b FROM BatchJob b WHERE b.status = :status ORDER BY b.startTime DESC")
    List<BatchJob> findByStatusOrderByStartTimeDesc(@Param("status") String status);

    void deleteByBatchId(String batchId);

    // Nuevos métodos para BatchJobService
    Optional<BatchJob> findByBatchIdAndFileName(String batchId, String fileName);

    List<BatchJob> findByBatchIdOrderByFileNameAsc(String batchId);

    List<BatchJob> findByStatusNot(String status);

    @Query("SELECT COUNT(b) FROM BatchJob b WHERE b.batchId = :batchId")
    long countByBatchId(@Param("batchId") String batchId);

    @Query("SELECT COUNT(b) FROM BatchJob b WHERE b.batchId = :batchId AND b.status = :status")
    long countByBatchIdAndStatus(@Param("batchId") String batchId, @Param("status") String status);
}

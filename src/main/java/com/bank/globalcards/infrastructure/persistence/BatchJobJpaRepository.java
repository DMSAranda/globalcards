package com.bank.globalcards.infrastructure.persistence;

import com.bank.globalcards.domain.models.BatchJob;
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
}

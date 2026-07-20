package com.bank.globalcards.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "batch_job_partition_progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_batch_file_partition",
                columnNames = {"batch_id", "file_name", "partition_index"}
        )
)
public class BatchJobPartitionProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "partition_index", nullable = false)
    private Integer partitionIndex;

    @Column(name = "processed_bytes")
    private Long processedBytes;

    @Column(name = "processed_records")
    private Long processedRecords;

    @Column(name = "valid_records")
    private Long validRecords;

    @Column(name = "invalid_records")
    private Long invalidRecords;

    @Column(name = "last_card_id")
    private String lastCardId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = "RUNNING";
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }

    public void markAsCompleted() {
        this.status = "COMPLETED";
    }
}

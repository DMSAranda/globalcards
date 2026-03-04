package com.bank.globalcards.domain.models;

import jakarta.persistence.*;
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
@Table(name = "batch_jobs")
public class BatchJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String batchId;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String status;
    
    @Column
    private Integer totalRecords;
    
    @Column
    private Integer processedRecords;
    
    @Column
    private Integer failedRecords;
    
    @Column
    private Instant startTime;
    
    @Column
    private Instant endTime;
    
    @Column(length = 1000)
    private String errorMessage;

    public void markAsCompleted() {
        this.status = "COMPLETED";
        this.endTime = Instant.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        this.endTime = Instant.now();
    }

    public void markAsRunning() {
        this.status = "RUNNING";
        this.startTime = Instant.now();
    }
}

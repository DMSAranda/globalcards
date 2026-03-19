package com.bank.globalcards.domain.models;

import com.bank.globalcards.infrastructure.persistence.entity.BatchJob;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BatchJobTest {

    @Test
    void shouldCreateBatchJob() {

        BatchJob job = BatchJob.builder()
                .batchId("1")
                .fileName("cards.csv")
                .totalRecords(40000L)
                .build();

        assertEquals("1", job.getBatchId());
        assertEquals("cards.csv", job.getFileName());
        assertEquals(40000L, job.getTotalRecords());
    }
}
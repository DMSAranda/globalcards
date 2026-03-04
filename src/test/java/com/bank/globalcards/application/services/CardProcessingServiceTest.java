package com.bank.globalcards.application.services;

import com.bank.globalcards.application.ports.out.BatchJobRepository;
import com.bank.globalcards.application.ports.out.CardEventPublisher;
import com.bank.globalcards.application.ports.out.CardStoragePort;
import com.bank.globalcards.domain.enums.CardStatus;
import com.bank.globalcards.domain.models.BatchJob;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.domain.models.CardFile;
import com.bank.globalcards.domain.models.CardUploadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardProcessingServiceTest {

    @Mock
    private CardStoragePort cardStoragePort;
    
    @Mock
    private CardEventPublisher cardEventPublisher;
    
    @Mock
    private BatchJobRepository batchJobRepository;

    private CardProcessingService cardProcessingService;

    @BeforeEach
    void setUp() {
        cardProcessingService = new CardProcessingService(
                cardStoragePort, cardEventPublisher, batchJobRepository);
    }

    @Test
    void processCardBatch_ValidCards_ShouldProcessSuccessfully() {
        // Given
        Card card1 = Card.builder()
                .cardId("card1")
                .pan("4111111111111111")
                .holder("John Doe")
                .status(CardStatus.PENDING)
                .build();

        Card card2 = Card.builder()
                .cardId("card2")
                .pan("5555555555554444")
                .holder("Jane Smith")
                .status(CardStatus.PENDING)
                .build();

        List<Card> cards = Arrays.asList(card1, card2);
        CardFile cardFile = CardFile.builder()
                .batchId("batch123")
                .fileName("test.csv")
                .build();

        CardUploadResult uploadResult = new CardUploadResult();
        when(cardStoragePort.uploadChunk(any(), any(), any())).thenReturn(uploadResult);

        // When
        CardUploadResult result = cardProcessingService.processCardBatch(cards, cardFile, 1);

        // Then
        assertEquals(2, result.getRecordsProcessed());
        assertEquals(0, result.getRecordsFailed());
        assertEquals(CardStatus.PROCESSED, card1.getStatus());
        assertEquals(CardStatus.PROCESSED, card2.getStatus());
        
        verify(cardEventPublisher, times(2)).publishCardOk(any());
        verify(cardEventPublisher, never()).publishCardKo(any());
        verify(cardStoragePort).uploadChunk(cards, "test.csv", 1);
    }

    @Test
    void processCardBatch_InvalidCards_ShouldMarkAsError() {
        // Given
        Card invalidCard1 = Card.builder()
                .cardId("card1")
                .pan("123") // Too short
                .holder("John Doe")
                .status(CardStatus.PENDING)
                .build();

        Card invalidCard2 = Card.builder()
                .cardId("card2")
                .pan("4111111111111111")
                .holder("") // Empty holder
                .status(CardStatus.PENDING)
                .build();

        List<Card> cards = Arrays.asList(invalidCard1, invalidCard2);
        CardFile cardFile = CardFile.builder()
                .batchId("batch123")
                .fileName("test.csv")
                .build();

        CardUploadResult uploadResult = new CardUploadResult();
        when(cardStoragePort.uploadChunk(any(), any(), any())).thenReturn(uploadResult);

        // When
        CardUploadResult result = cardProcessingService.processCardBatch(cards, cardFile, 1);

        // Then
        assertEquals(0, result.getRecordsProcessed());
        assertEquals(2, result.getRecordsFailed());
        assertEquals(CardStatus.ERROR, invalidCard1.getStatus());
        assertEquals(CardStatus.ERROR, invalidCard2.getStatus());
        
        verify(cardEventPublisher, never()).publishCardOk(any());
        verify(cardEventPublisher, times(2)).publishCardKo(any());
        verify(cardStoragePort).uploadChunk(cards, "test.csv", 1);
    }

    @Test
    void createBatchJob_ShouldSaveAndReturnJob() {
        // Given
        String batchId = "batch123";
        String fileName = "test.csv";
        Integer totalRecords = 100;
        
        BatchJob expectedJob = BatchJob.builder()
                .batchId(batchId)
                .fileName(fileName)
                .status("PENDING")
                .totalRecords(totalRecords)
                .processedRecords(0)
                .failedRecords(0)
                .build();
        
        when(batchJobRepository.save(any(BatchJob.class))).thenReturn(expectedJob);

        // When
        BatchJob result = cardProcessingService.createBatchJob(batchId, fileName, totalRecords);

        // Then
        assertNotNull(result);
        assertEquals(batchId, result.getBatchId());
        assertEquals(fileName, result.getFileName());
        assertEquals("PENDING", result.getStatus());
        assertEquals(totalRecords, result.getTotalRecords());
        assertEquals(0, result.getProcessedRecords());
        assertEquals(0, result.getFailedRecords());
        
        verify(batchJobRepository).save(any(BatchJob.class));
    }

    @Test
    void updateBatchJobStatus_ShouldUpdateExistingJob() {
        // Given
        String batchId = "batch123";
        String status = "COMPLETED";
        Integer processed = 95;
        Integer failed = 5;
        
        BatchJob existingJob = BatchJob.builder()
                .id(1L)
                .batchId(batchId)
                .fileName("test.csv")
                .status("RUNNING")
                .build();
        
        when(batchJobRepository.findByBatchId(batchId)).thenReturn(Optional.of(existingJob));
        when(batchJobRepository.save(any(BatchJob.class))).thenReturn(existingJob);

        // When
        cardProcessingService.updateBatchJobStatus(batchId, status, processed, failed);

        // Then
        assertEquals(status, existingJob.getStatus());
        assertEquals(processed, existingJob.getProcessedRecords());
        assertEquals(failed, existingJob.getFailedRecords());
        assertNotNull(existingJob.getEndTime());
        
        verify(batchJobRepository).findByBatchId(batchId);
        verify(batchJobRepository).save(existingJob);
    }

    @Test
    void updateBatchJobStatus_JobNotFound_ShouldThrowException() {
        // Given
        String batchId = "nonexistent";
        when(batchJobRepository.findByBatchId(batchId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cardProcessingService.updateBatchJobStatus(batchId, "COMPLETED", 100, 0)
        );
        
        assertEquals("Batch job not found: " + batchId, exception.getMessage());
        verify(batchJobRepository).findByBatchId(batchId);
        verify(batchJobRepository, never()).save(any());
    }
}

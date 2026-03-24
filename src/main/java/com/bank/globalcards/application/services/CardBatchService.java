package com.bank.globalcards.application.services;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.domain.models.Card;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardBatchService {

    private final CardValidationService validationService;
    private final CardStorageService storageService;
    private final CardEventService eventService;
    private final BatchMetricsService metricsService;
    private final BatchJobService batchJobService;

    @Transactional
    public void processChunk(
            List<Card> cards,
            String fileName,
            String batchId,
            Integer partitionNumber
    ) {
        Instant startTime = Instant.now();
        
        log.info("Processing chunk for file {} partition {} with {} cards", 
                fileName, partitionNumber, cards.size());

        // Iniciar timer para métricas
        var timerSample = metricsService.startChunkTimer();

        try {
            // Validación mejorada usando el CardValidationService (ahora con CardSchemaValidator interno)
            Map<Boolean, List<Card>> partitionedCards = cards.stream()
                    .collect(Collectors.partitioningBy(validationService::isValid));

            List<Card> validCards = partitionedCards.get(true);
            List<Card> invalidCards = partitionedCards.get(false);

            // Procesar cards válidos
            if (!validCards.isEmpty()) {
                try {
                    storageService.storeChunk(validCards, fileName, partitionNumber);
                    eventService.publishOk(validCards, batchId, fileName, partitionNumber);
                    metricsService.incrementValid(validCards.size(), partitionNumber);
                    log.debug("Processed {} valid cards for partition {}", validCards.size(), partitionNumber);
                    
                } catch (Exception e) {
                    log.error("Error processing valid cards for partition {}: {}", partitionNumber, e.getMessage(), e);
                    metricsService.recordProcessingError("storage", e.getClass().getSimpleName());
                    throw e;
                }
            }

            // Procesar cards inválidos
            if (!invalidCards.isEmpty()) {
                try {
                    eventService.publishKo(invalidCards, batchId, fileName, partitionNumber);
                    metricsService.incrementInvalid(invalidCards.size(), partitionNumber);
                    log.debug("Processed {} invalid cards for partition {}", invalidCards.size(), partitionNumber);
                    
                } catch (Exception e) {
                    log.error("Error publishing KO events for partition {}: {}", partitionNumber, e.getMessage(), e);
                    metricsService.recordProcessingError("kafka", e.getClass().getSimpleName());
                    // No lanzar excepción para no detener el procesamiento
                }
            }

            // Guardar checkpoint de progreso
            try {
                batchJobService.saveBatchJobProgress(
                        batchId, fileName, partitionNumber,
                        0, // bytes procesados (no disponible aquí)
                        cards.size(),
                        validCards.size(),
                        invalidCards.size(),
                        cards.isEmpty() ? null : cards.get(cards.size() - 1).getCardId()
                );
            } catch (Exception e) {
                log.error("Error saving batch job progress for partition {}: {}", partitionNumber, e.getMessage(), e);
                // No detener el procesamiento por error en checkpoint
            }

            // Detener timer y registrar métricas
            metricsService.stopChunkTimer(timerSample, partitionNumber);

            Duration processingTime = Duration.between(startTime, Instant.now());
            log.info("Completed chunk processing for file {} partition {}: {} total, {} valid, {} invalid, time {}ms", 
                    fileName, partitionNumber, cards.size(), validCards.size(), invalidCards.size(), 
                    processingTime.toMillis());

        } catch (Exception e) {
            // Registrar error en métricas
            metricsService.recordProcessingError("chunk_processing", e.getClass().getSimpleName());
            log.error("Error processing chunk for file {} partition {}: {}", fileName, partitionNumber, e.getMessage(), e);
            throw e;
        }
    }

    // Getters para inyección en otros componentes si es necesario
    public BatchMetricsService getMetricsService() {
        return metricsService;
    }

    public BatchJobService getBatchJobService() {
        return batchJobService;
    }
}

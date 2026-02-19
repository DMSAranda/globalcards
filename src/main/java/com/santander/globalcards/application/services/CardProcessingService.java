package com.santander.globalcards.application.services;

import com.santander.globalcards.domain.models.Card;
import com.santander.globalcards.domain.models.CardFile;
import com.santander.globalcards.domain.models.CardUploadResult;
import com.santander.globalcards.domain.records.CardEvent;
import com.santander.globalcards.domain.enums.CardStatus;
import com.santander.globalcards.application.ports.out.CardStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Servicio principal que orquesta el procesamiento de tarjetas
 * Casos de uso principales del dominio
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CardProcessingService {

    private final CardStoragePort cardStoragePort;
    // TODO: Add Kafka producer port
    // TODO: Add metadata repository port

    /**
     * Procesa un lote de tarjetas desde un fichero
     * @param cards Lista de tarjetas a procesar
     * @param cardFile Metadatos del fichero
     * @param partNumber Número de parte para multipart
     * @return Resultado del procesamiento
     */
    public CardUploadResult processCardBatch(List<Card> cards, CardFile cardFile, int partNumber) {
        log.info("Processing batch of {} cards from file {}, part {}", 
                cards.size(), cardFile.getFileName(), partNumber);

        int processed = 0;
        int failed = 0;

        for (Card card : cards) {
            try {
                // Validar y procesar tarjeta
                if (validateCard(card)) {
                    card.markAsProcessed();
                    processed++;
                    
                    // Publicar evento OK
                    publishCardEvent(card, cardFile, cardFile.getFileName(), partNumber, CardStatus.PROCESSED);
                } else {
                    card.markAsError();
                    failed++;
                    
                    // Publicar evento KO
                    publishCardEvent(card, cardFile, cardFile.getFileName(), partNumber, CardStatus.ERROR);
                }
            } catch (Exception e) {
                card.markAsError();
                failed++;
                log.error("Error processing card {}: {}", card.getCardId(), e.getMessage());
            }
        }

        // Subir chunk a S3
        CardUploadResult result = cardStoragePort.uploadChunk(cards, cardFile.getFileName(), partNumber);
        result.setRecordsProcessed(processed);
        result.setRecordsFailed(failed);

        log.info("Batch processing completed. Processed: {}, Failed: {}", processed, failed);
        return result;
    }

    /**
     * Valida una tarjeta según reglas del negocio
     */
    private boolean validateCard(Card card) {
        return card.getPan() != null && 
               card.getPan().length() >= 13 && 
               card.getPan().length() <= 19 &&
               card.getHolder() != null && 
               !card.getHolder().trim().isEmpty();
    }

    /**
     * Publica evento a Kafka
     */
    private void publishCardEvent(Card card, CardFile cardFile, String fileName, int partNumber, CardStatus status) {
        CardEvent event = new CardEvent(
            card.getCardId(),
            card.getPan(),
            card.getHolder(),
            status,
            "BATCH_PROCESSOR",
            cardFile.getBatchId(),
            fileName,
            partNumber,
            Instant.now()
        );
        
        // TODO: Implementar kafka producer
        log.debug("Card event published: {}", event);
    }
}

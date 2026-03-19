package com.bank.globalcards.domain.exceptions;

import lombok.Getter;

/**
 * Excepción lanzada cuando ocurre un error durante el procesamiento de un batch.
 * Representa errores relacionados con la ejecución, configuración o estado del batch.
 */
@Getter
public class BatchProcessingException extends CardProcessingException {
    
    private final String batchId;
    private final String fileName;
    
    public BatchProcessingException(String message) {
        super(message);
        this.batchId = null;
        this.fileName = null;
    }
    
    public BatchProcessingException(String message, String batchId) {
        super(String.format("Batch %s: %s", batchId, message));
        this.batchId = batchId;
        this.fileName = null;
    }
    
    public BatchProcessingException(String message, String batchId, String fileName) {
        super(String.format("Batch %s, file %s: %s", batchId, fileName, message));
        this.batchId = batchId;
        this.fileName = fileName;
    }
    
    public BatchProcessingException(String message, String batchId, String fileName, Throwable cause) {
        super(String.format("Batch %s, file %s: %s", batchId, fileName, message), cause);
        this.batchId = batchId;
        this.fileName = fileName;
    }
    
    public boolean hasBatchId() {
        return batchId != null;
    }
    
    public boolean hasFileName() {
        return fileName != null;
    }
}

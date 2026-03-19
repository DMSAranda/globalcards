package com.bank.globalcards.domain.exceptions;

import lombok.Getter;

/**
 * Excepción lanzada cuando ocurre un error durante operaciones de almacenamiento.
 * Representa errores relacionados con persistencia de datos, base de datos o almacenamiento.
 */
@Getter
public class StorageException extends CardProcessingException {
    
    private final String operation;
    private final String entity;
    
    public StorageException(String message) {
        super(message);
        this.operation = null;
        this.entity = null;
    }
    
    public StorageException(String message, String operation) {
        super(String.format("Storage operation %s failed: %s", operation, message));
        this.operation = operation;
        this.entity = null;
    }
    
    public StorageException(String message, String operation, String entity) {
        super(String.format("Storage operation %s failed for entity %s: %s", operation, entity, message));
        this.operation = operation;
        this.entity = entity;
    }
    
    public StorageException(String message, String operation, String entity, Throwable cause) {
        super(String.format("Storage operation %s failed for entity %s: %s", operation, entity, message), cause);
        this.operation = operation;
        this.entity = entity;
    }
    
    public boolean hasOperation() {
        return operation != null;
    }
    
    public boolean hasEntity() {
        return entity != null;
    }
}

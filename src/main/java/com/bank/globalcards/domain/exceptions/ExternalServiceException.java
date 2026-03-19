package com.bank.globalcards.domain.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Excepción lanzada cuando ocurre un error en servicios externos.
 * Representa errores relacionados con S3, Kafka, bases de datos externas o APIs de terceros.
 */
@Getter
@RequiredArgsConstructor
public class ExternalServiceException extends CardProcessingException {
    
    private final String serviceName;
    private final String operation;
    private final Boolean isTemporary;
    
    public ExternalServiceException(String message) {
        super(message);
        this.serviceName = null;
        this.operation = null;
        this.isTemporary = null;
    }
    
    public ExternalServiceException(String message, String serviceName) {
        super(String.format("External service %s: %s", serviceName, message));
        this.serviceName = serviceName;
        this.operation = null;
        this.isTemporary = null;
    }
    
    public ExternalServiceException(String message, String serviceName, String operation) {
        super(String.format("External service %s operation %s: %s", serviceName, operation, message));
        this.serviceName = serviceName;
        this.operation = operation;
        this.isTemporary = null;
    }
    
    public ExternalServiceException(String message, String serviceName, String operation, Boolean isTemporary) {
        super(String.format("External service %s operation %s: %s", serviceName, operation, message));
        this.serviceName = serviceName;
        this.operation = operation;
        this.isTemporary = isTemporary;
    }
    
    public ExternalServiceException(String message, String serviceName, String operation, Boolean isTemporary, Throwable cause) {
        super(String.format("External service %s operation %s: %s", serviceName, operation, message), cause);
        this.serviceName = serviceName;
        this.operation = operation;
        this.isTemporary = isTemporary;
    }
    
    public boolean hasServiceName() {
        return serviceName != null;
    }
    
    public boolean hasOperation() {
        return operation != null;
    }
    
    public boolean isTemporary() {
        return Boolean.TRUE.equals(isTemporary);
    }
}

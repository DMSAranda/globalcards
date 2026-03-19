package com.bank.globalcards.domain.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Excepción lanzada cuando una tarjeta no cumple las reglas de validación del dominio.
 * Representa errores de validación de negocio: formato, longitud, checksum, etc.
 */
@Getter
@RequiredArgsConstructor
public class CardValidationException extends CardProcessingException {
    
    private final String cardId;
    private final String field;
    
    public CardValidationException(String message) {
        super(message);
        this.cardId = null;
        this.field = null;
    }
    
    public CardValidationException(String message, String cardId) {
        super(message);
        this.cardId = cardId;
        this.field = null;
    }
    
    public CardValidationException(String message, String cardId, String field) {
        super(String.format("Card %s validation failed for field '%s': %s", 
                cardId != null ? cardId : "unknown", field, message));
        this.cardId = cardId;
        this.field = field;
    }
    
    public CardValidationException(String message, String cardId, String field, Throwable cause) {
        super(String.format("Card %s validation failed for field '%s': %s", 
                cardId != null ? cardId : "unknown", field, message), cause);
        this.cardId = cardId;
        this.field = field;
    }
    
    public boolean hasCardId() {
        return cardId != null;
    }
    
    public boolean hasField() {
        return field != null;
    }
}

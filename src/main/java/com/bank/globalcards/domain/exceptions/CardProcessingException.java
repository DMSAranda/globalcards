package com.bank.globalcards.domain.exceptions;

import lombok.Getter;

/**
 * Excepción base para todos los errores de procesamiento de tarjetas.
 * Es la excepción raíz del dominio para problemas relacionados con el procesamiento de cards.
 */
@Getter
public class CardProcessingException extends RuntimeException {
    
    private final String message;
    
    public CardProcessingException(String message) {
        super(message);
        this.message = message;
    }
    
    public CardProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
}

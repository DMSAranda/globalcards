package com.bank.globalcards.domain.exceptions;

/**
 * Excepción base para todos los errores de procesamiento de tarjetas.
 * Es la excepción raíz del dominio para problemas relacionados con el procesamiento de cards.
 */
public class CardProcessingException extends RuntimeException {

    public CardProcessingException(String message) {
        super(message);
    }

    public CardProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

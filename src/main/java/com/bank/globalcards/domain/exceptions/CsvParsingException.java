package com.bank.globalcards.domain.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Excepción lanzada cuando ocurre un error durante el parsing de archivos CSV.
 * Representa errores específicos del formato CSV, estructura de datos o parsing.
 */
@Getter
@RequiredArgsConstructor
public class CsvParsingException extends CardProcessingException {
    
    private final Integer lineNumber;
    
    public CsvParsingException(String message) {
        super(message);
        this.lineNumber = null;
    }
    
    public CsvParsingException(String message, Throwable cause) {
        super(message, cause);
        this.lineNumber = null;
    }
    
    public CsvParsingException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }
    
    public CsvParsingException(String message, int lineNumber, Throwable cause) {
        super(message, cause);
        this.lineNumber = lineNumber;
    }
    
    public boolean hasLineNumber() {
        return lineNumber != null;
    }
    
    @Override
    public String getMessage() {
        String baseMessage = super.getMessage();
        if (hasLineNumber()) {
            return String.format("Line %d: %s", lineNumber, baseMessage);
        }
        return baseMessage;
    }
}

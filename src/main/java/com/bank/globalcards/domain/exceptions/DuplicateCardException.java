package com.bank.globalcards.domain.exceptions;

public class DuplicateCardException extends CardProcessingException {

    public DuplicateCardException(String cardId) {
        super("Card already processed: " + cardId);
    }

}
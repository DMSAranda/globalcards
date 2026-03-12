package com.bank.globalcards.domain.exceptions;

public class DuplicateCardException extends RuntimeException {

    public DuplicateCardException(String cardId) {
        super("Card already processed: " + cardId);
    }

}
package com.bank.globalcards.application.services;

import com.bank.globalcards.domain.models.Card;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardValidationServiceTest {

    private final CardValidationService service = new CardValidationService();

    @Test
    void shouldValidateCorrectNumericCard() {

        Card card = Card.builder()
                .cardId("123")
                .pan("1234567812345678")
                .holder("987654321")
                .build();

        assertTrue(service.isValid(card));
    }

    @Test
    void shouldRejectCardIdWithLetters() {

        Card card = Card.builder()
                .cardId("ABC123")
                .pan("1234567812345678")
                .holder("987654321")
                .build();

        assertFalse(service.isValid(card));
    }

    @Test
    void shouldRejectPanWithLetters() {

        Card card = Card.builder()
                .cardId("1")
                .pan("1234ABCD5678")
                .holder("987654321")
                .build();

        assertFalse(service.isValid(card));
    }

    @Test
    void shouldRejectHolderWithLetters() {

        Card card = Card.builder()
                .cardId("1")
                .pan("1234567812345678")
                .holder("ABC123")
                .build();

        assertFalse(service.isValid(card));
    }
}
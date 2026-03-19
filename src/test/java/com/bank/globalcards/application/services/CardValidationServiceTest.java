package com.bank.globalcards.application.services;

import com.bank.globalcards.domain.models.Card;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardValidationServiceTest {

    private final CardValidationService service = new CardValidationService();

    @Test
    void shouldValidateCorrectNumericCard() {

        Card card = Card.builder()
                .cardId("123456")
                .pan("4532015112830366")
                .holder("123456789")
                .build();

        assertTrue(service.isValid(card));
    }

    @Test
    void shouldRejectCardIdWithLetters() {

        Card card = Card.builder()
                .cardId("ABC123")
                .pan("4532015112830366")
                .holder("123456789")
                .build();

        assertFalse(service.isValid(card));
    }

    @Test
    void shouldRejectPanWithLetters() {

        Card card = Card.builder()
                .cardId("CARD1")
                .pan("1234ABCD5678")
                .holder("123456789")
                .build();

        assertFalse(service.isValid(card));
    }

    @Test
    void shouldRejectHolderWithLetters() {

        Card card = Card.builder()
                .cardId("CARD1")
                .pan("4532015112830366")
                .holder("ABC")
                .build();

        assertFalse(service.isValid(card));
    }
}
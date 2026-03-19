package com.bank.globalcards.domain.models;

import com.bank.globalcards.domain.enums.CardStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void shouldCreateCard() {

        Card card = Card.builder()
                .cardId("1567")
                .pan("1234567890123456")
                .holder("6589658")
                .status(CardStatus.VALIDATED)
                .build();

        assertEquals("1567", card.getCardId());
        assertEquals("6589658", card.getHolder());
        assertEquals(CardStatus.VALIDATED, card.getStatus());
    }
}
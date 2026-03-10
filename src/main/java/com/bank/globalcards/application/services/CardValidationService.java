package com.bank.globalcards.application.services;

import com.bank.globalcards.domain.enums.CardStatus;
import com.bank.globalcards.domain.models.Card;
import org.springframework.stereotype.Service;

@Service
public class CardValidationService {

    public Card validate(Card card) {

        if (card.getPan() == null || card.getPan().length() < 13) {
            card.setStatus(CardStatus.ERROR);
        } else {
            card.setStatus(CardStatus.VALIDATED);
        }

        return card;
    }
}
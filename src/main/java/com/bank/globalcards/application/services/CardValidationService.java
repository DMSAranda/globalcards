package com.bank.globalcards.application.services;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.domain.enums.CardStatus;
import com.bank.globalcards.domain.models.Card;
import org.springframework.stereotype.Service;

@Service
public class CardValidationService {

    public boolean isValid(CardDto card) {

        if (card.getPan() == null || card.getPan().length() < 13) {
            return false;
        }

        return true;
    }

    public CardDto validate(CardDto card) {

        if (isValid(card)) {
            card.setStatus(CardStatus.VALIDATED);
        } else {
            card.setStatus(CardStatus.ERROR);
        }

        return card;
    }
}
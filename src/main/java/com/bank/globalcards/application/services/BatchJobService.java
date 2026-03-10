package com.bank.globalcards.application.services;

import com.bank.globalcards.domain.models.Card;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatchJobService {

    public List<Card> validate(List<Card> cards) {

        return cards.stream()
                .filter(card -> card.getPan() != null)
                .toList();

    }
}
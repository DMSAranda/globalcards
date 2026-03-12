package com.bank.globalcards.application.services;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.application.ports.out.CardRepository;
import com.bank.globalcards.domain.models.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardPersistenceService {

    private final CardRepository cardRepository;

    public void saveAll(List<Card> cards) {

        cardRepository.saveAll(cards);
    }

}
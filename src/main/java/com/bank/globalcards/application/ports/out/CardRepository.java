package com.bank.globalcards.application.ports.out;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.domain.enums.CardStatus;

import java.util.List;
import java.util.Optional;

public interface CardRepository {

    Card save(Card card);

    List<Card> saveAll(List<Card> cards);

    Optional<Card> findById(String cardId);

    List<Card> findByBatchId(String batchId);

    List<Card> findByStatus(CardStatus status);
}
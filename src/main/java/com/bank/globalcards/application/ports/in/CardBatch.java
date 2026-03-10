package com.bank.globalcards.application.port.in;

import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.domain.models.CardFile;

import java.util.List;

public interface CardBatch {

    void processBatch(List<Card> cards, CardFile file, int partition);

}
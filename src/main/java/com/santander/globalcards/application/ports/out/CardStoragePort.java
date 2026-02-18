package com.santander.globalcards.application.ports.out;

import com.santander.globalcards.domain.model.Card;

import java.util.List;

public interface CardStoragePort {

    void uploadChunk(List<Card> cards, String fileName, int partNumber);

}

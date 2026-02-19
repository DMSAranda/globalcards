package com.bank.globalcards.application.ports.out;

import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.domain.models.CardUploadResult;

import java.util.List;

public interface CardStoragePort {

    CardUploadResult uploadChunk(List<Card> cards, String fileName, int partNumber);

}

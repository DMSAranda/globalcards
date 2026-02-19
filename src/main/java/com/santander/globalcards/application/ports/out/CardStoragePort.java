package com.santander.globalcards.application.ports.out;

import com.santander.globalcards.domain.models.Card;
import com.santander.globalcards.domain.models.CardUploadResult;

import java.util.List;

public interface CardStoragePort {

    CardUploadResult uploadChunk(List<Card> cards, String fileName, int partNumber);

}

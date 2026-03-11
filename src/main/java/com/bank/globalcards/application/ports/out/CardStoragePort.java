package com.bank.globalcards.application.ports.out;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.domain.models.CardUploadResult;

import java.util.List;

public interface CardStoragePort {

    CardUploadResult uploadChunk(List<CardDto> cards, String fileName, int partNumber);

}

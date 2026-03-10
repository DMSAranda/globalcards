package com.bank.globalcards.application.services;

import com.bank.globalcards.application.ports.out.CardStoragePort;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.domain.models.CardUploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardStorageService {

    private final CardStoragePort cardStoragePort;

    public CardUploadResult storeChunk(List<Card> cards, String fileName, int partNumber) {

        return cardStoragePort.uploadChunk(cards, fileName, partNumber);

    }
}
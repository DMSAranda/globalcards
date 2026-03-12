package com.bank.globalcards.application.services;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.application.ports.out.CardStoragePort;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.domain.models.CardUploadResult;
import com.bank.globalcards.infrastructure.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardStorageService {

    private final CardStoragePort cardStoragePort;
    private final CardMapper cardMapper;

    public void storeChunk(List<Card> cards, String fileName, int partNumber) {

        List<CardDto> cardDtos = cards.stream()
                        .map(cardMapper::toDto)
                        .toList();

        cardStoragePort.uploadChunk(cardDtos, fileName, partNumber);

    }
}
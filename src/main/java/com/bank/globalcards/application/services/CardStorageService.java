package com.bank.globalcards.application.services;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.application.ports.out.CardStoragePort;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.infrastructure.redsys.RedsysFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardStorageService {

    private final CardStoragePort cardStoragePort;
    private final RedsysFormatter redsysFormatter;

    public void storeChunk(List<Card> cards, String fileName, int partitionNumber) {

        List<String> lines = cards.stream()
                .map(redsysFormatter::format)
                .toList();

        cardStoragePort.uploadChunk(lines, fileName, partitionNumber);

    }
}
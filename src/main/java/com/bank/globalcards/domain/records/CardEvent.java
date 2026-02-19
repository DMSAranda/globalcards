package com.bank.globalcards.domain.records;

import com.bank.globalcards.domain.enums.CardStatus;
import java.time.Instant;

public record CardEvent(

        String cardId,
        String pan,
        String holder,
        CardStatus status,
        String source,
        String batchId,
        String fileName,
        int partNumber,
        Instant timestamp
) {}


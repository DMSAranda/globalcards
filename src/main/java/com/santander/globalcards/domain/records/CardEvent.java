package com.santander.globalcards.domain.records;

import com.santander.globalcards.domain.enums.CardStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record CardEvent(

        String cardId,
        String pan,
        String holder,
        CardStatus status,
        String source,
        Instant timestamp
) {}


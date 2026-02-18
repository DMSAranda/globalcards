package com.santander.globalcards.domain.records;

import java.time.LocalDateTime;
import java.util.UUID;

public record CardEvent(
        UUID eventId,
        String cardNumber,
        String status,
        LocalDateTime timestamp
) {}

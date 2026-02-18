package com.santander.globalcards.application.ports.out;

import com.santander.globalcards.domain.records.CardEvent;

public interface CardEventPublisher {
    void publishCardOk(CardEvent event);
    void publishCardKo(CardEvent event);
}

package com.bank.globalcards.application.ports.out;

import com.bank.globalcards.domain.records.CardEvent;

public interface CardEventPublisher {
    void publishCardOk(CardEvent event);
    void publishCardKo(CardEvent event);
}

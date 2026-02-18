package org.santander.cards.application.ports.out;

import org.santander.cards.domain.model.CardEvent;

public interface CardEventPublisher {
    void publishCardOk(CardEvent event);
    void publishCardKo(CardEvent event);
}

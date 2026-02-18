package org.santander.cards.infrastructure.kafka;

import org.santander.cards.domain.model.CardEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.santander.cards.application.ports.out.CardEventPublisher;

@Component
public class KafkaCardEventProducer implements CardEventPublisher {

    private final KafkaTemplate<String, CardEvent> kafkaTemplate;

    public KafkaCardEventProducer(KafkaTemplate<String, CardEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishCardOk(CardEvent event) { kafkaTemplate.send("cardsok", event);
    }

    @Override
    public void publishCardKo(CardEvent event) {
        kafkaTemplate.send("cardsko", event);
    }
}

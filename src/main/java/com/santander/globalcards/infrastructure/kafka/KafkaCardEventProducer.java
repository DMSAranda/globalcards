package com.santander.globalcards.infrastructure.kafka;

import com.santander.globalcards.domain.records.CardEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.santander.globalcards.application.ports.out.CardEventPublisher;

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

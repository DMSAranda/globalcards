package com.bank.globalcards.infrastructure.kafka;

import com.bank.globalcards.application.ports.out.CardEventPublisher;
import com.bank.globalcards.domain.records.CardEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaCardEventProducer implements CardEventPublisher {

    private final KafkaTemplate<String, CardEvent> kafkaTemplate;

    @Override
    public void publishCardOk(CardEvent event) { kafkaTemplate.send("cardsok", event);
    }

    @Override
    public void publishCardKo(CardEvent event) {
        kafkaTemplate.send("cardsko", event);
    }
}

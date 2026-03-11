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

    private static final String TOPIC_OK = "cards-ok";
    private static final String TOPIC_KO = "cards-ko";
    private static final String TOPIC_DLQ = "cards-dlq";

    @Override
    public void publishCardOk(CardEvent event) {
        kafkaTemplate.send(TOPIC_OK, buildKey(event), event);
    }

    @Override
    public void publishCardKo(CardEvent event) {
        kafkaTemplate.send(TOPIC_KO, buildKey(event), event);
    }

    @Override
    public void publishCardDlq(CardEvent event) {
        kafkaTemplate.send(TOPIC_DLQ, buildKey(event), event);
    }

    private String buildKey(CardEvent event) {
        return event.batchId() + "-" + event.cardId() + "-" + event.partitionNumber();
    }
}
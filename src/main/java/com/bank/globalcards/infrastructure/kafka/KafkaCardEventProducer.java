package com.bank.globalcards.infrastructure.kafka;

import com.bank.globalcards.application.ports.out.CardEventPublisher;
import com.bank.globalcards.domain.exceptions.EventPublishingException;
import com.bank.globalcards.domain.records.CardEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaCardEventProducer implements CardEventPublisher {

    private final KafkaTemplate<String, CardEvent> kafkaTemplate;

    private static final String TOPIC_OK = "cards-ok";
    private static final String TOPIC_KO = "cards-ko";
    private static final String TOPIC_DLQ = "cards-dlq";

    @Override
    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackPublishCardOk")
    public void publishCardOk(CardEvent event) {
        try {
            kafkaTemplate.send(TOPIC_OK, buildKey(event), event);
            if (log.isDebugEnabled()) {
                log.debug("Published OK event for card: {}", event.cardId());
            }
        } catch (Exception e) {
            log.error("Failed to publish OK event for card: {}", event.cardId(), e);
            throw new EventPublishingException("Failed to publish OK event", "CARD_OK", TOPIC_OK, event.cardId(), e);
        }
    }

    @Override
    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackPublishCardKo")
    public void publishCardKo(CardEvent event) {
        try {
            kafkaTemplate.send(TOPIC_KO, buildKey(event), event);
            if (log.isDebugEnabled()) {
                log.debug("Published KO event for card: {}", event.cardId());
            }
        } catch (Exception e) {
            log.error("Failed to publish KO event for card: {}", event.cardId(), e);
            throw new EventPublishingException("Failed to publish KO event", "CARD_KO", TOPIC_KO, event.cardId(), e);
        }
    }

    @Override
    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackPublishCardDlq")
    public void publishCardDlq(CardEvent event) {
        try {
            kafkaTemplate.send(TOPIC_DLQ, buildKey(event), event);
            if (log.isDebugEnabled()) {
                log.debug("Published DLQ event for card: {}", event.cardId());
            }
        } catch (Exception e) {
            log.error("Failed to publish DLQ event for card: {}", event.cardId(), e);
            throw new EventPublishingException("Failed to publish DLQ event", "CARD_DLQ", TOPIC_DLQ, event.cardId(), e);
        }
    }

    // Métodos fallback
    public void fallbackPublishCardOk(CardEvent event, Exception e) {
        log.error("Kafka circuit breaker activated for OK event - card: {}, error: {}", 
                event.cardId(), e.getMessage());
        throw new EventPublishingException("Kafka circuit breaker activated", "CARD_OK", TOPIC_OK, event.cardId(), e);
    }

    public void fallbackPublishCardKo(CardEvent event, Exception e) {
        log.error("Kafka circuit breaker activated for KO event - card: {}, error: {}", 
                event.cardId(), e.getMessage());
        throw new EventPublishingException("Kafka circuit breaker activated", "CARD_KO", TOPIC_KO, event.cardId(), e);
    }

    public void fallbackPublishCardDlq(CardEvent event, Exception e) {
        log.error("Kafka circuit breaker activated for DLQ event - card: {}, error: {}", 
                event.cardId(), e.getMessage());
        throw new EventPublishingException("Kafka circuit breaker activated", "CARD_DLQ", TOPIC_DLQ, event.cardId(), e);
    }

    private String buildKey(CardEvent event) {
        return event.batchId() + "-" + event.cardId() + "-" + event.partitionNumber();
    }
}

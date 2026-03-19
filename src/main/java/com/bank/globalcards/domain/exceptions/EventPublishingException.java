package com.bank.globalcards.domain.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Excepción lanzada cuando ocurre un error durante la publicación de eventos.
 * Representa errores relacionados con Kafka, mensajería o sistemas de eventos.
 */
@Getter
@RequiredArgsConstructor
public class EventPublishingException extends CardProcessingException {
    
    private final String eventType;
    private final String topic;
    private final String cardId;
    
    public EventPublishingException(String message) {
        super(message);
        this.eventType = null;
        this.topic = null;
        this.cardId = null;
    }
    
    public EventPublishingException(String message, String eventType) {
        super(String.format("Event type %s: %s", eventType, message));
        this.eventType = eventType;
        this.topic = null;
        this.cardId = null;
    }
    
    public EventPublishingException(String message, String eventType, String topic) {
        super(String.format("Event type %s to topic %s: %s", eventType, topic, message));
        this.eventType = eventType;
        this.topic = topic;
        this.cardId = null;
    }
    
    public EventPublishingException(String message, String eventType, String topic, String cardId) {
        super(String.format("Event type %s to topic %s for card %s: %s", 
                eventType, topic, cardId, message));
        this.eventType = eventType;
        this.topic = topic;
        this.cardId = cardId;
    }
    
    public EventPublishingException(String message, String eventType, String topic, String cardId, Throwable cause) {
        super(String.format("Event type %s to topic %s for card %s: %s", 
                eventType, topic, cardId, message), cause);
        this.eventType = eventType;
        this.topic = topic;
        this.cardId = cardId;
    }
    
    public boolean hasEventType() {
        return eventType != null;
    }
    
    public boolean hasTopic() {
        return topic != null;
    }
    
    public boolean hasCardId() {
        return cardId != null;
    }
}

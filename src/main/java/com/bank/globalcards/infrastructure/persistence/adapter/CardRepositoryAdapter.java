package com.bank.globalcards.infrastructure.persistence.adapter;

import com.bank.globalcards.application.ports.out.CardRepository;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.domain.enums.CardStatus;
import com.bank.globalcards.application.mapper.CardMapper;
import com.bank.globalcards.infrastructure.persistence.entity.CardEntity;
import com.bank.globalcards.infrastructure.persistence.repository.JpaCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CardRepositoryAdapter implements CardRepository {

    private final JpaCardRepository jpaRepository;
    private final CardMapper mapper;

    @Override
    public Card save(Card card, String fileName, String batchId, Integer partitionNumber) {

        CardEntity entity = mapper.toEntity(card, fileName, batchId, partitionNumber);
        CardEntity saved = jpaRepository.save(entity);

        return mapper.toDomain(saved);
    }

    @Override
    public List<Card> saveAll(List<Card> cards, String fileName, String batchId, Integer partitionNumber) {

        List<CardEntity> entities = cards.stream()
                .map(card -> mapper.toEntity(card, fileName, batchId, partitionNumber))
                .toList();

        List<CardEntity> saved = jpaRepository.saveAll(entities);

        return saved.stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Card> findById(String cardId) {

        return jpaRepository.findById(cardId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Card> findByBatchId(String batchId) {

        return jpaRepository.findByBatchId(batchId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Card> findByStatus(CardStatus status) {

        return jpaRepository.findByStatus(status.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
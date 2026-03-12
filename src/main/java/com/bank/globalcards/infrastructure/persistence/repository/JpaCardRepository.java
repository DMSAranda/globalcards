package com.bank.globalcards.infrastructure.persistence.repository;

import com.bank.globalcards.infrastructure.persistence.entity.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaCardRepository extends JpaRepository<CardEntity, String> {

    List<CardEntity> findByBatchId(String batchId);

    List<CardEntity> findByStatus(String status);
}
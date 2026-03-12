package com.bank.globalcards.infrastructure.persistence.entity;

import com.bank.globalcards.domain.enums.CardStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "cards",
        indexes = {
                @Index(name = "idx_cards_batch_id", columnList = "batch_id"),
                @Index(name = "idx_cards_status", columnList = "status"),
                @Index(name = "idx_cards_partition", columnList = "partition_number")
        }
)
public class CardEntity {

    @Id
    @Column(name = "card_id", nullable = false, length = 50)
    private String cardId;

    @Column(name = "pan", nullable = false, length = 32)
    private String pan;

    @Column(name = "holder", length = 100)
    private String holder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private CardStatus status;

    @Column(name = "batch_id", length = 100)
    private String batchId;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "partition_number")
    private Integer partitionNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
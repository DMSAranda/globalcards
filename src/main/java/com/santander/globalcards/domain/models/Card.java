package com.santander.globalcards.domain.models;

import com.santander.globalcards.domain.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    private String cardId;
    private String pan;
    private String holder;
    private CardStatus status;

    public void markAsProcessed() {
        this.status = CardStatus.PROCESSED;
    }

    public void markAsError() {
        this.status = CardStatus.ERROR;
    }
}

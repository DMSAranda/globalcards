package com.santander.globalcards.domain.model;

import com.santander.globalcards.domain.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Card {

    private String cardId;
    private String holder;
    private String brand;
    private String pan;
    private CardStatus status;
}

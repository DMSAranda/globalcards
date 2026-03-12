package com.bank.globalcards.application.dtos;

import com.bank.globalcards.domain.enums.CardStatus;
import lombok.*;

@Getter
@AllArgsConstructor
@Builder
public class CardDto {
    private String cardId;
    private String pan;
    private String holder;
    private CardStatus status;
}

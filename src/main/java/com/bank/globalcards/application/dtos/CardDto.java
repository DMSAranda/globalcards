package com.bank.globalcards.application.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardDto {
    private String cardId;
    private String pan;
    private String holder;
    private String status;
}

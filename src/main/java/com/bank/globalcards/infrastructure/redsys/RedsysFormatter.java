package com.bank.globalcards.infrastructure.redsys;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.domain.models.Card;
import org.springframework.stereotype.Component;

@Component
public class RedsysFormatter {

    public String format(Card card) {

        StringBuilder sb = new StringBuilder(64);

        sb.append(padRight(card.getPan(), 16));
        sb.append(padRight(card.getHolder(), 20));
        sb.append(padRight(card.getCardId(), 12));
        sb.append("EUR");

        return sb.toString();
    }

    private String padRight(String value, int length) {

        if (value == null) value = "";

        if (value.length() >= length) {
            return value.substring(0, length);
        }

        return String.format("%-" + length + "s", value);
    }
}
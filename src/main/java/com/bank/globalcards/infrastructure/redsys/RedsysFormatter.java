package com.bank.globalcards.infrastructure.redsys;

import com.bank.globalcards.domain.models.Card;
import org.springframework.stereotype.Component;

@Component
public class RedsysFormatter {

    public String format(Card card) {

        StringBuilder sb = new StringBuilder(64);

        sb.append(padLeftZeros(card.getPan(), 16));
        sb.append(padLeftZeros(card.getHolder(), 20));
        sb.append(padLeftZeros(card.getCardId(), 12));
        sb.append("978");

        return sb.toString();
    }

    private String padLeftZeros(String value, int length) {

        if (value == null) value = "";

        if (value.length() >= length) {
            return value.substring(0, length);
        }

        StringBuilder sb = new StringBuilder(length);

        for (int i = value.length(); i < length; i++) {
            sb.append('0');
        }

        sb.append(value);

        return sb.toString();
    }
}
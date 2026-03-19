package com.bank.globalcards.application.services;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.domain.enums.CardStatus;
import com.bank.globalcards.domain.models.Card;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@Slf4j
public class CardValidationService {

    // Patrones de validación
    private static final Pattern CARD_ID_PATTERN = Pattern.compile("^\\d{3,50}$");
    private static final Pattern HOLDER_NAME_PATTERN = Pattern.compile("^\\d{2,100}$");
    private static final Pattern PAN_PATTERN = Pattern.compile("^\\d{13,19}$");

    public boolean isValid(Card card) {
        try {
            // Validar que el card no sea nulo
            if (card == null) {
                log.warn("Card object is null");
                return false;
            }

            // Validar PAN
            if (card.getPan() == null || card.getPan().length() < 13) {
                log.warn("Invalid PAN for card {}: null or too short", card.getCardId());
                return false;
            }

            // Validar formato del PAN
            String cleanPan = card.getPan().replaceAll("[\\s-]", "");
            if (!PAN_PATTERN.matcher(cleanPan).matches()) {
                log.warn("Invalid PAN format for card {}: {}", card.getCardId(), card.getPan());
                return false;
            }

            // Validar checksum Luhn
            if (!isValidLuhn(cleanPan)) {
                log.warn("Invalid PAN checksum for card {}: {}", card.getCardId(), card.getPan());
                return false;
            }

            // Validar holder
            if (card.getHolder() == null || card.getHolder().trim().isEmpty()) {
                log.warn("Invalid holder for card {}: null or empty", card.getCardId());
                return false;
            }

            // Validar formato del holder
            if (!HOLDER_NAME_PATTERN.matcher(card.getHolder()).matches()) {
                log.warn("Invalid holder format for card {}: {}", card.getCardId(), card.getHolder());
                return false;
            }

            // Validar cardId
            if (card.getCardId() == null || card.getCardId().trim().isEmpty()) {
                log.warn("Invalid cardId for card: null or empty");
                return false;
            }

            // Validar formato del cardId (solo números)
            if (!CARD_ID_PATTERN.matcher(card.getCardId()).matches()) {
                log.warn("Invalid cardId format for card: {}", card.getCardId());
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Error validating card {}: {}", card.getCardId(), e.getMessage(), e);
            return false;
        }
    }

    private boolean isValidLuhn(String pan) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = pan.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(pan.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return sum % 10 == 0;
    }
}
package com.bank.globalcards.application.services;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.domain.enums.CardStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class CsvParsingService {

    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT
            .builder()
            .setHeader("cardId", "pan", "holder")
            .setSkipHeaderRecord(true)
            .setIgnoreHeaderCase(true)
            .setTrim(true)
            .setQuote('"')
            .setEscape('\\')
            .setIgnoreEmptyLines(true)
            .setAllowDuplicateHeaderNames(false)
            .build();

    private static final Set<String> REQUIRED_HEADERS = new HashSet<>(Arrays.asList("cardId", "pan", "holder"));
    private static final int MAX_FIELD_LENGTH = 1000;

    public CardDto parseCardFromCsvLine(String line, int lineNumber) throws CsvParsingException {
        if (line == null || line.trim().isEmpty()) {
            throw new CsvParsingException("Empty line at line " + lineNumber);
        }

        try (StringReader reader = new StringReader(line);
             CSVParser parser = CSVParser.parse(reader, CSV_FORMAT)) {

            if (parser.getRecords().isEmpty()) {
                throw new CsvParsingException("No valid CSV record found at line " + lineNumber);
            }

            CSVRecord record = parser.getRecords().get(0);

            // Validar número de campos
            if (record.size() < 3) {
                throw new CsvParsingException(
                    String.format("Insufficient fields at line %d. Expected 3, got %d", lineNumber, record.size())
                );
            }

            // Extraer campos (sin validaciones de negocio - eso lo hace CardValidationService)
            String cardId = record.get("cardId");
            String pan = record.get("pan");
            String holder = record.get("holder");

            return CardDto.builder()
                    .cardId(cardId != null ? cardId.trim() : "")
                    .pan(pan != null ? pan.trim() : "")
                    .holder(holder != null ? holder.trim() : "")
                    .status(CardStatus.PENDING)
                    .build();

        } catch (CsvParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new CsvParsingException("Error parsing CSV line " + lineNumber + ": " + e.getMessage(), e);
        }
    }

    public static class CsvParsingException extends Exception {
        public CsvParsingException(String message) {
            super(message);
        }

        public CsvParsingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

package com.bank.globalcards.infrastructure.batch.reader;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.application.services.CsvParsingService;
import com.bank.globalcards.domain.enums.CardStatus;
import com.bank.globalcards.infrastructure.batch.config.DynamicBatchConfig;
import com.bank.globalcards.infrastructure.s3.S3Properties;
import com.bank.globalcards.infrastructure.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class S3CardItemReader implements ItemReader<CardDto>, ItemStream {

    private final S3Service s3Service;
    private final S3Properties s3Properties;
    private final CsvParsingService csvParsingService;
    private final DynamicBatchConfig dynamicBatchConfig;
    private final String fileName;
    private final long startByte;
    private final long endByte;

    private BufferedReader reader;
    private long currentBytePosition;
    private boolean finished = false;
    private int lineNumber = 0;
    private int validRecords = 0;
    private int invalidRecords = 0;

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            String key = s3Properties.getS3().getInputFolder() + fileName;

            log.info(
                    "Opening S3 reader for file {} (bytes {} - {})",
                    fileName, startByte, endByte
            );

            InputStream inputStream =
                    s3Service.downloadFileRange(key, startByte, endByte + 8192);

            reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8),
                    dynamicBatchConfig.getS3BufferSize() // 256 KB buffer
            );

            currentBytePosition = startByte;

            // Si no empezamos desde el principio, saltar a la siguiente línea completa
            if (startByte > 0) {
                String skipped = reader.readLine();
                if (skipped != null) {
                    currentBytePosition += skipped.length() + 1;
                    lineNumber++;
                }
            }

            log.info("Reader opened successfully for file {}, starting at line {}", fileName, lineNumber);

        } catch (Exception e) {
            throw new ItemStreamException("Error opening S3 reader for file " + fileName, e);
        }
    }

    @Override
    public CardDto read() throws Exception {
        if (finished) {
            return null;
        }

        // Importante: no devolvemos null por una línea inválida,
        // porque `null` en un ItemReader significa fin del input.
        while (true) {
            String line = reader.readLine();

            if (line == null) {
                finished = true;
                log.info(
                        "End of file reached for {}. Valid records: {}, Invalid records: {}",
                        fileName,
                        validRecords,
                        invalidRecords
                );
                return null;
            }

            currentBytePosition += line.length() + 1;
            lineNumber++;

            // Verificar si hemos alcanzado el límite de bytes para esta partición
            if (currentBytePosition > endByte) {
                finished = true;
                log.info(
                        "Byte limit reached for file {}. Valid records: {}, Invalid records: {}",
                        fileName,
                        validRecords,
                        invalidRecords
                );
                return null;
            }

            try {
                // Parsear la línea con el nuevo servicio de parsing
                CardDto card = csvParsingService.parseCardFromCsvLine(line, lineNumber);
                validRecords++;

                if (log.isTraceEnabled()) {
                    log.trace("Successfully parsed line {}: cardId={}", lineNumber, card.getCardId());
                }

                return card;
            } catch (CsvParsingService.CsvParsingException e) {
                invalidRecords++;
                log.warn("Invalid CSV format at line {} in file {}: {}", lineNumber, fileName, e.getMessage());
                // Saltar la línea inválida y continuar leyendo.
            } catch (Exception e) {
                log.error(
                        "Unexpected error reading line {} from file {}: {}",
                        lineNumber,
                        fileName,
                        e.getMessage(),
                        e
                );
                throw e;
            }
        }
    }

    @Override
    public void close() throws ItemStreamException {
        try {
            if (reader != null) {
                reader.close();
            }
            
            log.info("Reader closed for file {}. Total lines processed: {}, Valid: {}, Invalid: {}", 
                    fileName, lineNumber, validRecords, invalidRecords);

        } catch (Exception e) {
            throw new ItemStreamException("Error closing S3 reader for file " + fileName, e);
        }
    }

    // Getters para métricas
    public int getValidRecords() {
        return validRecords;
    }

    public int getInvalidRecords() {
        return invalidRecords;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
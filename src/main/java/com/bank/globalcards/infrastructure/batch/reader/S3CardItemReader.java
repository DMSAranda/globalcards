package com.bank.globalcards.infrastructure.batch.reader;

import com.bank.globalcards.application.dtos.CardDto;
import com.bank.globalcards.domain.enums.CardStatus;
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

    private final String fileName;
    private final long startByte;
    private final long endByte;

    private BufferedReader reader;

    private long currentBytePosition;
    private boolean finished = false;

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
                    262144 // 256 KB buffer
            );

            currentBytePosition = startByte;

            if (startByte > 0) {

                String skipped = reader.readLine();

                if (skipped != null) {
                    currentBytePosition += skipped.length() + 1;
                }
            }

        } catch (Exception e) {
            throw new ItemStreamException("Error opening S3 reader", e);
        }
    }

    @Override
    public CardDto read() throws Exception {

        if (finished) {
            return null;
        }

        String line = reader.readLine();

        if (line == null) {
            finished = true;
            return null;
        }

        currentBytePosition += line.length() + 1;

        if (currentBytePosition > endByte) {
            finished = true;
        }

        return parseLine(line);
    }

    private CardDto parseLine(String line) {

        int firstComma = line.indexOf(',');

        if (firstComma == -1) {
            log.warn("Invalid CSV line skipped: {}", line);
            return null;
        }

        int secondComma = line.indexOf(',', firstComma + 1);

        if (secondComma == -1) {
            log.warn("Invalid CSV line skipped: {}", line);
            return null;
        }

        String cardId = line.substring(0, firstComma);
        String pan = line.substring(firstComma + 1, secondComma);
        String holder = line.substring(secondComma + 1);

        return CardDto.builder()
                .cardId(cardId)
                .pan(pan)
                .holder(holder)
                .status(CardStatus.PENDING)
                .build();
    }

    @Override
    public void close() throws ItemStreamException {

        try {

            if (reader != null) {
                reader.close();
            }

        } catch (Exception e) {
            throw new ItemStreamException("Error closing S3 reader", e);
        }
    }
}
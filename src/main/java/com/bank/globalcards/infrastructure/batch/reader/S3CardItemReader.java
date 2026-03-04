package com.bank.globalcards.infrastructure.batch.reader;

import com.bank.globalcards.domain.enums.CardStatus;
import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.infrastructure.s3.S3Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

@Slf4j
public class S3CardItemReader implements ItemReader<Card>, ItemStream {

    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final String fileName;
    private final int start;
    private final int end;

    private BufferedReader reader;
    private int currentLine = 0;
    private boolean headerSkipped = false;

    public S3CardItemReader(S3Client s3Client,
                            S3Properties s3Properties,
                            String fileName,
                            int start,
                            int end) {
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
        this.fileName = fileName;
        this.start = start;
        this.end = end;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            String inputKey = s3Properties.getS3().getInputFolder() + fileName;

            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(s3Properties.getS3().getBucket())
                    .key(inputKey)
                    .build();

            InputStream inputStream = s3Client.getObject(request);
            reader = new BufferedReader(new InputStreamReader(inputStream));

            log.info("S3CardItemReader abierto para rango {} - {}", start, end);

        } catch (Exception e) {
            throw new ItemStreamException("Error abriendo reader S3", e);
        }
    }

    @Override
    public Card read() throws Exception {

        if (reader == null) {
            return null;
        }

        String line;

        while ((line = reader.readLine()) != null) {

            currentLine++;

            // Saltar header solo una vez
            if (!headerSkipped) {
                headerSkipped = true;
                if (line.toLowerCase().contains("cardid")) {
                    continue;
                }
            }

            // Saltar hasta llegar al inicio de la partición
            if (currentLine < start) {
                continue;
            }

            // Parar cuando superamos el rango
            if (currentLine > end) {
                return null;
            }

            try {
                return parseCardFromLine(line, currentLine);
            } catch (Exception e) {
                log.warn("Error parseando línea {}: {}", currentLine, e.getMessage());
            }
        }

        return null;
    }

    @Override
    public void close() throws ItemStreamException {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) {
            throw new ItemStreamException("Error cerrando reader S3", e);
        }
    }

    private Card parseCardFromLine(String line, int lineNumber) {

        String[] fields = line.split(",");

        if (fields.length < 3) {
            log.warn("Formato inválido en línea {}: {}", lineNumber, line);
            return null;
        }

        return Card.builder()
                .cardId(fields[0].trim())
                .pan(fields[1].trim())
                .holder(fields[2].trim())
                .status(CardStatus.PENDING)
                .build();
    }
}
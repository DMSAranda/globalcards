package com.bank.globalcards.infrastructure.batch.reader;

import com.bank.globalcards.domain.models.Card;
import com.bank.globalcards.infrastructure.s3.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
@RequiredArgsConstructor
public class S3CardItemReader implements ItemReader<Card>, ItemStream {

    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final String fileName;
    private final long startByte;
    private final long endByte;

    private BufferedReader reader;
    private boolean firstLineSkipped = false;

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {

            String key = s3Properties.getS3().getInputFolder() + fileName;

            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(s3Properties.getS3().getBucket())
                    .key(key)
                    .range("bytes=" + startByte + "-" + endByte)
                    .build();

            InputStream inputStream = s3Client.getObject(request);

            reader = new BufferedReader(new InputStreamReader(inputStream));

            log.info("Reader opened for bytes {} - {}", startByte, endByte);

        } catch (Exception e) {
            throw new ItemStreamException("Error opening S3 reader", e);
        }
    }

    @Override
    public Card read() throws Exception {

        String line = reader.readLine();

        if (line == null) {
            return null;
        }

        if (!firstLineSkipped && startByte > 0) {
            firstLineSkipped = true;
            line = reader.readLine();
            if (line == null) return null;
        }

        return parseLine(line);
    }

    private Card parseLine(String line) {

        String[] fields = line.split(",");

        return Card.builder()
                .cardId(fields[0].trim())
                .pan(fields[1].trim())
                .holder(fields[2].trim())
                .status(com.bank.globalcards.domain.enums.CardStatus.PENDING)
                .build();
    }
}
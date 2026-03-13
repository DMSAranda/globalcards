package com.bank.globalcards.infrastructure.s3;

import com.bank.globalcards.application.ports.out.CardStoragePort;
import com.bank.globalcards.domain.models.CardUploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class S3CardStorageAdapter implements CardStoragePort {

    private final S3Service s3Service;

    @Override
    public CardUploadResult uploadChunk(List<String> lines, String fileName, int partitionNumber) {

        String chunkName = fileName + ".part" + String.format("%03d", partitionNumber);

        String content = String.join("\n", lines);

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        s3Service.uploadFile(chunkName, inputStream, bytes.length);

        return new CardUploadResult(
                chunkName,
                lines.size(),
                0
        );
    }
}
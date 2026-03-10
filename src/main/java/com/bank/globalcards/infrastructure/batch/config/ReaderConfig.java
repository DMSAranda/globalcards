package com.bank.globalcards.infrastructure.batch.config;

import com.bank.globalcards.infrastructure.batch.reader.S3CardItemReader;
import com.bank.globalcards.infrastructure.s3.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
public class ReaderConfig {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Bean
    @StepScope
    public S3CardItemReader cardItemReader(
            @Value("#{stepExecutionContext['fileName']}") String fileName,
            @Value("#{stepExecutionContext['startByte']}") long startByte,
            @Value("#{stepExecutionContext['endByte']}") long endByte) {

        return new S3CardItemReader(
                s3Client,
                s3Properties,
                fileName,
                startByte,
                endByte
        );
    }
}
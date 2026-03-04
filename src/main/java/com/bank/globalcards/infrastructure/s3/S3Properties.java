package com.bank.globalcards.infrastructure.s3;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "aws")
public class

S3Properties {
    private String region;
    private Credentials credentials;
    private S3 s3;

    @Data
    public static class Credentials {
        private String accessKey;
        private String secretKey;
    }

    @Data
    public static class S3 {
        private String bucket;
        private String inputFolder;
        private String outputFolder;
    }
}

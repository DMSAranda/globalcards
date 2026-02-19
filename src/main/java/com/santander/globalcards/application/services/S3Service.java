package com.santander.globalcards.application.services;

import com.santander.globalcards.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public List<String> listInputFiles() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(s3Properties.getBucket())
                .prefix(s3Properties.getInputFolder())
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        return response.contents().stream()
                .map(object -> object.key())
                .toList();
    }

    public InputStream downloadFile(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(key)
                .build();

        return s3Client.getObject(request);
    }

    public void uploadFile(String fileName, byte[] content) {
        String outputKey = s3Properties.getOutputFolder() + fileName;
        
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(outputKey)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(content));
        log.info("File uploaded successfully: {}", outputKey);
    }

    public void moveFile(String sourceKey, String destinationKey) {
        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(s3Properties.getBucket())
                .sourceKey(sourceKey)
                .destinationBucket(s3Properties.getBucket())
                .destinationKey(destinationKey)
                .build();

        s3Client.copyObject(copyRequest);

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(sourceKey)
                .build();

        s3Client.deleteObject(deleteRequest);
        log.info("File moved from {} to {}", sourceKey, destinationKey);
    }

    public boolean fileExists(String key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(key)
                    .build();
            
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
}

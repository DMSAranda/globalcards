package com.bank.globalcards.infrastructure.s3;

import com.bank.globalcards.application.ports.out.BatchJobRepository;
import com.bank.globalcards.domain.exceptions.ExternalServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @CircuitBreaker(name = "s3Service", fallbackMethod = "fallbackListInputFiles")
    public List<String> listInputFiles() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(s3Properties.getS3().getBucket())
                .prefix(s3Properties.getS3().getInputFolder())
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        return response.contents().stream()
                .map(S3Object::key)
                .toList();
    }

    @CircuitBreaker(name = "s3Service", fallbackMethod = "fallbackDownloadFile")
    public InputStream downloadFile(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Properties.getS3().getBucket())
                .key(key)
                .build();

        return s3Client.getObject(request);
    }

    @CircuitBreaker(name = "s3Service", fallbackMethod = "fallbackDownloadFileRange")
    public InputStream downloadFileRange(String key, long startByte, long endByte) {

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Properties.getS3().getBucket())
                .key(key)
                .range("bytes=" + startByte + "-" + endByte)
                .build();

        return s3Client.getObject(request);
    }

    @CircuitBreaker(name = "s3Service", fallbackMethod = "fallbackUploadFile")
    public void uploadFile(String fileName, InputStream content, long contentLength) {
        String outputKey = s3Properties.getS3().getOutputFolder() + fileName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.getS3().getBucket())
                .key(outputKey)
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(content, contentLength));
    }

    @CircuitBreaker(name = "s3Service", fallbackMethod = "fallbackMoveFile")
    public void moveFile(String sourceKey, String destinationKey) {
        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(s3Properties.getS3().getBucket())
                .sourceKey(sourceKey)
                .destinationBucket(s3Properties.getS3().getBucket())
                .destinationKey(destinationKey)
                .build();

        s3Client.copyObject(copyRequest);

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(s3Properties.getS3().getBucket())
                .key(sourceKey)
                .build();

        s3Client.deleteObject(deleteRequest);
        log.info("File moved from {} to {}", sourceKey, destinationKey);
    }

    @CircuitBreaker(name = "s3Service", fallbackMethod = "fallbackFileExists")
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(s3Properties.getS3().getBucket())
                    .key(key)
                    .build();
            
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    // Métodos fallback
    public List<String> fallbackListInputFiles(Exception e) {
        log.error("S3 circuit breaker activated for listInputFiles: {}", e.getMessage());
        return List.of(); // Retorna lista vacía
    }

    public InputStream fallbackDownloadFile(String key, Exception e) {
        log.error("S3 circuit breaker activated for downloadFile {}: {}", key, e.getMessage());
        throw new ExternalServiceException("S3 service unavailable", "S3", "downloadFile", true, e);
    }

    public InputStream fallbackDownloadFileRange(String key, long startByte, long endByte, Exception e) {
        log.error("S3 circuit breaker activated for downloadFileRange {} ({}-{}): {}", 
                key, startByte, endByte, e.getMessage());
        throw new ExternalServiceException("S3 service unavailable for range download", "S3", "downloadFileRange", true, e);
    }

    public void fallbackUploadFile(String fileName, InputStream content, long contentLength, Exception e) {
        log.error("S3 circuit breaker activated for uploadFile {}: {}", fileName, e.getMessage());
        throw new ExternalServiceException("S3 service unavailable for upload", "S3", "uploadFile", true, e);
    }

    public void fallbackMoveFile(String sourceKey, String destinationKey, Exception e) {
        log.error("S3 circuit breaker activated for moveFile {} -> {}: {}", 
                sourceKey, destinationKey, e.getMessage());
        throw new ExternalServiceException("S3 service unavailable for move operation", "S3", "moveFile", true, e);
    }

    public boolean fallbackFileExists(String key, Exception e) {
        log.error("S3 circuit breaker activated for fileExists {}: {}", key, e.getMessage());
        return false; // Asumir que no existe para evitar procesamiento
    }
}

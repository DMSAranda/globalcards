package com.bank.globalcards.infrastructure.batch.partition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import com.bank.globalcards.infrastructure.s3.S3Properties;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class S3FilePartitioner implements Partitioner {

    private final S3Client s3Client;
    private final S3Properties s3Properties;
    @Value("#{jobParameters['fileName']}")
    private String fileName;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        Map<String, ExecutionContext> partitions = new HashMap<>();

        if (fileName == null || fileName.isBlank()) {
            log.warn("Missing job parameter fileName. No partitions will be created.");
            return partitions;
        }

        String normalizedFileName = fileName.trim();
        String key = s3Properties.getS3().getInputFolder() + normalizedFileName;

        try {
            HeadObjectResponse metadata = s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(s3Properties.getS3().getBucket())
                            .key(key)
                            .build()
            );

            long fileSize = metadata.contentLength();
            int partitionsPerFile = calculatePartitionsForSingleFile(fileSize, gridSize);

            log.info(
                    "Creating {} partitions for file {} (size: {} bytes)",
                    partitionsPerFile,
                    normalizedFileName,
                    fileSize
            );

            long partitionSize = Math.max(1L, fileSize / partitionsPerFile);
            long start = 0;

            for (int partitionIndex = 0; partitionIndex < partitionsPerFile; partitionIndex++) {
                long end = (partitionIndex == partitionsPerFile - 1)
                        ? Math.max(0L, fileSize - 1)
                        : Math.min(fileSize - 1, start + partitionSize - 1);

                ExecutionContext context = new ExecutionContext();
                context.putString("fileName", normalizedFileName);
                context.putLong("startByte", start);
                context.putLong("endByte", end);
                context.putInt("partitionIndex", partitionIndex);
                context.putString("fileKey", key);
                context.putLong("fileSize", fileSize);

                partitions.put("partition-" + partitionIndex, context);
                log.debug("Partition {} for file {}: {} - {}", partitionIndex, normalizedFileName, start, end);

                start = end + 1;
            }
        } catch (Exception e) {
            log.error("Error creating partitions for file {}: {}", normalizedFileName, e.getMessage(), e);
        }

        log.info("Total partitions created: {}", partitions.size());
        return partitions;
    }

    private int calculatePartitionsForSingleFile(long fileSize, int gridSize) {
        int partitions = Math.max(1, gridSize);

        if (fileSize > 1_000_000) {
            partitions = Math.max(2, partitions);
        }

        if (fileSize > 10_000_000) {
            partitions = Math.max(4, partitions);
        }

        return partitions;
    }
}
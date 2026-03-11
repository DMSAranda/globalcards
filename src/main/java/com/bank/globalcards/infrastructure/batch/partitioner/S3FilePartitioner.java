package com.bank.globalcards.infrastructure.batch.partition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import com.bank.globalcards.infrastructure.s3.S3Properties;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3FilePartitioner implements Partitioner {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        Map<String, ExecutionContext> partitions = new HashMap<>();

        String bucket = s3Properties.getS3().getBucket();
        String key = s3Properties.getS3().getInputFolder() + "cards.csv";

        HeadObjectResponse metadata = s3Client.headObject(
                HeadObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );

        long fileSize = metadata.contentLength();

        long partitionSize = fileSize / gridSize;

        long start = 0;
        long end = partitionSize;

        for (int i = 0; i < gridSize; i++) {

            ExecutionContext context = new ExecutionContext();

            context.putString("fileName", "cards.csv");
            context.putLong("startByte", start);

            if (i == gridSize - 1) {
                end = fileSize;
            }

            context.putLong("endByte", end);

            context.putInt("partitionIndex", i);

            partitions.put("partition-" + i, context);

            log.info("Partition {} -> {} - {}", i, start, end);

            start = end + 1;
            end = start + partitionSize;
        }

        return partitions;
    }
}
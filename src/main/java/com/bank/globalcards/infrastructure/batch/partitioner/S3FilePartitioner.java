package com.bank.globalcards.infrastructure.batch.partitioner;

import com.bank.globalcards.infrastructure.s3.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class S3FilePartitioner implements Partitioner {

    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final String fileName;
    private final int partitionsCount;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitions = new HashMap<>();
        
        try {
            // Calcular líneas totales en el archivo
            int totalLines = countLinesInFile();
            int linesPerPartition = Math.max(1, totalLines / partitionsCount);
            
            log.info("Total lines: {}, Partitions: {}, Lines per partition: {}", 
                    totalLines, partitionsCount, linesPerPartition);

            for (int i = 0; i < partitionsCount; i++) {
                ExecutionContext context = new ExecutionContext();
                
                int startLine = i * linesPerPartition + 1; // +1 para saltar header
                int endLine = (i == partitionsCount - 1) ? totalLines : (i + 1) * linesPerPartition;
                
                context.putString("fileName", fileName);
                context.putInt("partitionIndex", i);
                context.putInt("startLine", startLine);
                context.putInt("endLine", endLine);
                context.putInt("totalLines", totalLines);
                
                partitions.put("partition-" + i, context);
                
                log.debug("Created partition {}: lines {}-{}", i, startLine, endLine);
            }
            
        } catch (Exception e) {
            log.error("Error creating partitions for file: {}", fileName, e);
            throw new RuntimeException("Failed to partition file: " + fileName, e);
        }
        
        return partitions;
    }

    private int countLinesInFile() {
        try {
            String inputKey = s3Properties.getS3().getInputFolder() + fileName;
            
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(s3Properties.getS3().getBucket())
                    .key(inputKey)
                    .build();

            int lineCount = 0;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(s3Client.getObject(request)))) {
                
                while (reader.readLine() != null) {
                    lineCount++;
                }
            }
            
            return lineCount;
            
        } catch (Exception e) {
            log.error("Error counting lines in file: {}", fileName, e);
            throw new RuntimeException("Failed to count lines in file: " + fileName, e);
        }
    }
}

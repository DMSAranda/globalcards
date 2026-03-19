package com.bank.globalcards.infrastructure.batch.partition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import com.bank.globalcards.infrastructure.s3.S3Properties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3FilePartitioner implements Partitioner {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        Map<String, ExecutionContext> partitions = new HashMap<>();

        // Listar todos los archivos CSV en la carpeta de input
        List<String> csvFiles = listCsvFiles();
        
        if (csvFiles.isEmpty()) {
            log.warn("No CSV files found in input folder: {}", s3Properties.getS3().getInputFolder());
            return partitions;
        }

        log.info("Found {} CSV files to process", csvFiles.size());

        int partitionIndex = 0;
        
        for (String fileName : csvFiles) {
            
            String key = s3Properties.getS3().getInputFolder() + fileName;
            
            try {
                HeadObjectResponse metadata = s3Client.headObject(
                        HeadObjectRequest.builder()
                                .bucket(s3Properties.getS3().getBucket())
                                .key(key)
                                .build()
                );

                long fileSize = metadata.contentLength();
                
                // Calcular particiones para este archivo
                int partitionsPerFile = calculatePartitionsPerFile(fileSize, gridSize, csvFiles.size());
                
                log.info("Creating {} partitions for file {} (size: {} bytes)", 
                        partitionsPerFile, fileName, fileSize);

                // Crear particiones para este archivo
                long partitionSize = fileSize / partitionsPerFile;
                long start = 0;
                long end = partitionSize;

                for (int i = 0; i < partitionsPerFile; i++) {

                    ExecutionContext context = new ExecutionContext();

                    context.putString("fileName", fileName);
                    context.putLong("startByte", start);

                    if (i == partitionsPerFile - 1) {
                        end = fileSize;
                    }

                    context.putLong("endByte", end);
                    context.putInt("partitionIndex", partitionIndex);
                    context.putString("fileKey", key);
                    context.putLong("fileSize", fileSize);

                    partitions.put("partition-" + partitionIndex, context);

                    log.debug("Partition {} for file {}: {} - {}", 
                            partitionIndex, fileName, start, end);

                    start = end + 1;
                    end = start + partitionSize;
                    partitionIndex++;
                }
                
            } catch (Exception e) {
                log.error("Error processing file {}: {}", fileName, e.getMessage(), e);
                // Continuar con otros archivos
            }
        }

        log.info("Total partitions created: {}", partitions.size());
        return partitions;
    }

    private List<String> listCsvFiles() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(s3Properties.getS3().getBucket())
                .prefix(s3Properties.getS3().getInputFolder())
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        
        return response.contents().stream()
                .filter(s3Object -> s3Object.key().toLowerCase().endsWith(".csv"))
                .map(s3Object -> {
                    String fullPath = s3Object.key();
                    String folder = s3Properties.getS3().getInputFolder();
                    return fullPath.startsWith(folder) ? fullPath.substring(folder.length()) : fullPath;
                })
                .filter(fileName -> !fileName.isEmpty())
                .collect(Collectors.toList());
    }

    private int calculatePartitionsPerFile(long fileSize, int totalGridSize, int fileCount) {
        // Distribuir particiones equitativamente entre archivos
        int basePartitionsPerFile = Math.max(1, totalGridSize / fileCount);
        
        // Ajustar según tamaño del archivo (mínimo 2 particiones para archivos > 1MB)
        if (fileSize > 1_000_000) {
            basePartitionsPerFile = Math.max(2, basePartitionsPerFile);
        }
        
        // Para archivos muy grandes, más particiones
        if (fileSize > 10_000_000) {
            basePartitionsPerFile = Math.max(4, basePartitionsPerFile);
        }
        
        return basePartitionsPerFile;
    }
}
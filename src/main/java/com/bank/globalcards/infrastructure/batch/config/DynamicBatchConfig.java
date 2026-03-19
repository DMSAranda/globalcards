package com.bank.globalcards.infrastructure.batch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class DynamicBatchConfig {

    @Value("${batch.processing.chunk-size:1000}")
    private int chunkSize;

    @Value("${batch.processing.grid-size:10}")
    private int gridSize;

    @Value("${batch.processing.max-retries:3}")
    private int maxRetries;

    @Value("${batch.processing.skip-limit:50}")
    private int skipLimit;

    @Value("${s3.buffer-size:262144}")
    private int s3BufferSize;

    @Value("${s3.multipart-threshold:16777216}")
    private long s3MultipartThreshold;

    // Método para calcular chunk size óptimo basado en tamaño de archivo
    public int calculateOptimalChunkSize(long fileSize) {
        // Para archivos pequeños, chunks más pequeños
        if (fileSize < 1_000_000) { // < 1MB
            return 100;
        }
        
        // Para archivos medianos
        if (fileSize < 10_000_000) { // < 10MB
            return 500;
        }
        
        // Para archivos grandes, usar configuración por defecto
        return chunkSize;
    }

    // Método para calcular grid size óptimo basado en número de archivos
    public int calculateOptimalGridSize(int fileCount) {
        // Mínimo 5 particiones totales
        int minGrid = 5;
        
        // Máximo 20 particiones para no sobrecargar
        int maxGrid = 20;
        
        // Calcular basado en número de archivos (2 particiones por archivo como mínimo)
        int calculatedGrid = Math.max(minGrid, fileCount * 2);
        
        return Math.min(calculatedGrid, maxGrid);
    }
}

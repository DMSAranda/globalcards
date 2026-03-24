package com.bank.globalcards.infrastructure.batch.job;

import com.bank.globalcards.infrastructure.s3.S3Properties;
import com.bank.globalcards.infrastructure.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "globalcards.batch-orchestrator.enabled", havingValue = "true")
public class S3BatchOrchestratorRunner implements ApplicationRunner {

    private final S3Service s3Service;
    private final S3Properties s3Properties;
    private final JobLauncher jobLauncher;
    private final Job cardProcessingJob;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> inputKeys = s3Service.listInputFiles().stream()
                .filter(key -> key != null && key.toLowerCase().endsWith(".csv"))
                .toList();

        if (inputKeys.isEmpty()) {
            log.info("No CSV files found in input folder. Nothing to process.");
            return;
        }

        log.info("Starting sequential batch orchestration for {} files", inputKeys.size());

        for (String key : inputKeys) {
            String fileName = toRelativeFileName(key);
            String batchId = UUID.randomUUID().toString();

            JobExecution execution = jobLauncher.run(
                    cardProcessingJob,
                    new JobParametersBuilder()
                            .addString("batchId", batchId)
                            .addString("fileName", fileName)
                            .addLong("launchTime", System.currentTimeMillis())
                            .toJobParameters()
            );

            log.info(
                    "Finished file {} with batchId {} and status {}",
                    fileName,
                    batchId,
                    execution.getStatus()
            );
        }

        log.info("Sequential batch orchestration finished.");
    }

    private String toRelativeFileName(String key) {
        String prefix = s3Properties.getS3().getInputFolder();
        if (key.startsWith(prefix)) {
            return key.substring(prefix.length());
        }
        return key;
    }
}

package com.bank.globalcards.application.services;

import io.micrometer.core.instrument.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchMetricsService {

    private final MeterRegistry meterRegistry;

    public void incrementProcessed(int count, int partition) {

        Counter.builder("cards.processed.total")
                .tag("partition", String.valueOf(partition))
                .register(meterRegistry)
                .increment(count);
    }

    public void incrementValid(int count, int partition) {

        Counter.builder("cards.valid.total")
                .tag("partition", String.valueOf(partition))
                .register(meterRegistry)
                .increment(count);
    }

    public void incrementInvalid(int count, int partition) {

        Counter.builder("cards.invalid.total")
                .tag("partition", String.valueOf(partition))
                .register(meterRegistry)
                .increment(count);
    }

    public Timer.Sample startChunkTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopChunkTimer(Timer.Sample sample, int partition) {

        sample.stop(
                Timer.builder("batch.chunk.processing.time")
                        .tag("partition", String.valueOf(partition))
                        .register(meterRegistry)
        );
    }
}
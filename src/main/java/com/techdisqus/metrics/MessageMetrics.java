package com.techdisqus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MessageMetrics {

    private final Counter processedCounter;
    private final Counter failedCounter;
    private final Counter dlqCounter;

    public MessageMetrics(MeterRegistry registry) {
        this.processedCounter = Counter.builder("messages_processed_total")
                .description("Total number of successfully processed messages")
                .register(registry);

        this.failedCounter = Counter.builder("messages_failed_total")
                .description("Total number of failed messages")
                .register(registry);

        this.dlqCounter = Counter.builder("messages_dlq_total")
                .description("Total number of messages sent to DLQ")
                .register(registry);
    }

    public void incrementProcessed() {
        processedCounter.increment();
    }

    public void incrementFailed() {
        failedCounter.increment();
    }

    public void incrementDLQ() {
        dlqCounter.increment();
    }
}

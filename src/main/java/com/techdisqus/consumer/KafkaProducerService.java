package com.techdisqus.consumer;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service

public class KafkaProducerService {
    Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @CircuitBreaker(name = "kafkaPublisher", fallbackMethod = "publishToDLQ")
    public void publish(String topic, String message) {
        try {
            kafkaTemplate.send(topic, message).get(); // wait for acknowledgment
            logger.info("✅ Published to Kafka topic: {}" , topic);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // preserve interrupt flag
            throw new RuntimeException("Kafka publish interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Kafka publish failed", e);
        }
    }


    public void publishToDLQ(String topic, String message, Throwable t) {
        logger.error("⚠️ Kafka publish failed, sending to DLQ: {} " , message, t);
        // send message to RabbitMQ DLQ (or persist)
        // You could autowire RabbitTemplate and send to test-queue.dlq
    }
}


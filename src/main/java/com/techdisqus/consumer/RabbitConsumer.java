package com.techdisqus.consumer;



import com.rabbitmq.client.Channel;
import com.techdisqus.metrics.MessageMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RabbitConsumer {

    Logger logger = LoggerFactory.getLogger(RabbitConsumer.class);
    private final KafkaProducerService kafkaProducer;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    private MessageMetrics metrics;

    public RabbitConsumer(KafkaProducerService kafkaProducer, RabbitTemplate rabbitTemplate) {
        this.kafkaProducer = kafkaProducer;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "test-queue")
    public void receive(Message message, Channel channel) throws IOException {
        String msg = new String(message.getBody());
        logger.info("📩 Received: {}" , msg);

        try {
            kafkaProducer.publish("test-topic", msg);
            // ✅ ACK only if Kafka publish succeeds
            metrics.incrementProcessed();
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("❌ Kafka publish failed, sending to retry DLQ", e);
            rabbitTemplate.convertAndSend("app.exchange", "test-queue.retry", msg);
            metrics.incrementDLQ();
            // ❌ Reject message so it’s not stuck unacked
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

}


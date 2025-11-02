package com.techdisqus.config;



import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    private static final String MAIN_EXCHANGE = "app.exchange";
    private static final String MAIN_QUEUE = "test-queue";
    private static final String RETRY_QUEUE = "test-queue.retry";
    private static final String DLQ_QUEUE = "test-queue.dlq";

    @Bean
    public DirectExchange appExchange() {
        return new DirectExchange(MAIN_EXCHANGE);
    }

    @Bean
    public Queue mainQueue() {
        return QueueBuilder.durable(MAIN_QUEUE)
                .withArgument("x-dead-letter-exchange", MAIN_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RETRY_QUEUE)
                .build();
    }

    @Bean
    public Queue retryQueue() {
        // 30 seconds delay before retry
        return QueueBuilder.durable(RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", MAIN_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", MAIN_QUEUE)
                .withArgument("x-message-ttl", 30000)
                .build();
    }

    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    @Bean
    public Binding mainBinding() {
        return BindingBuilder.bind(mainQueue()).to(appExchange()).with(MAIN_QUEUE);
    }

    @Bean
    public Binding retryBinding() {
        return BindingBuilder.bind(retryQueue()).to(appExchange()).with(RETRY_QUEUE);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlqQueue()).to(appExchange()).with(DLQ_QUEUE);
    }
}

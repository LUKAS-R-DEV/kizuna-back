
package kizuna.audit.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "kizuna.exchange";
    public static final String AUDIT_QUEUE = "audit.queue";
    public static final String ROUTING_KEY = "event.audit";

    public static final String DLX = "audit.dlx";
    public static final String DLQ = "audit.dlq";

    // ======================
    // QUEUE PRINCIPAL
    // ======================
    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    // ======================
    // DEAD LETTER QUEUE
    // ======================
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    // ======================
    // EXCHANGE GLOBAL (MESMO DO CORE)
    // ======================
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // ======================
    // DEAD LETTER EXCHANGE
    // ======================
    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX);
    }

    // ======================
    // BINDING PRINCIPAL
    // ======================
    @Bean
    public Binding binding(Queue auditQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(auditQueue)
                .to(exchange)
                .with(ROUTING_KEY);
    }

    // ======================
    // BINDING DLQ
    // ======================
    @Bean
    public Binding dlqBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder
                .bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(DLQ);
    }
}
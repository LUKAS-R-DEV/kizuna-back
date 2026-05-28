package com.kizuna.data_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "kizuna.exchange";
    public static final String QUEUE = "data.events.queue";

    private final RabbitAdmin rabbitAdmin;

    public RabbitConfig(ConnectionFactory connectionFactory) {
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitAdmin rabbitAdmin() {
        return rabbitAdmin;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void removeOldBinding() {
        try {
            rabbitAdmin.removeBinding(new Binding(QUEUE, Binding.DestinationType.QUEUE, EXCHANGE, "event.#", null));
            System.out.println("[DATA-SERVICE] Removed old generic binding event.#");
        } catch (Exception e) {
            System.out.println("[DATA-SERVICE] Old binding event.# not found or already removed");
        }
    }

    // ======================
    // QUEUE
    // ======================
    @Bean
    public Queue dataQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    // ======================
    // EXCHANGE GLOBAL
    // ======================
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // ======================
    // BINDINGS ESPECÍFICOS (exclui event.audit)
    // ======================
    @Bean
    public Binding productionBinding(Queue dataQueue, TopicExchange exchange) {
        return BindingBuilder.bind(dataQueue).to(exchange).with("event.production.*");
    }

    @Bean
    public Binding productionOrderBinding(Queue dataQueue, TopicExchange exchange) {
        return BindingBuilder.bind(dataQueue).to(exchange).with("event.production_order");
    }

    @Bean
    public Binding inventoryBinding(Queue dataQueue, TopicExchange exchange) {
        return BindingBuilder.bind(dataQueue).to(exchange).with("event.inventory.#");
    }

    @Bean
    public Binding inspectionBinding(Queue dataQueue, TopicExchange exchange) {
        return BindingBuilder.bind(dataQueue).to(exchange).with("event.inspection.*");
    }

    @Bean
    public Binding recipeBinding(Queue dataQueue, TopicExchange exchange) {
        return BindingBuilder.bind(dataQueue).to(exchange).with("event.recipe.*");
    }

    @Bean
    public Binding notificationBinding(Queue dataQueue, TopicExchange exchange) {
        return BindingBuilder.bind(dataQueue).to(exchange).with("event.notification");
    }
}
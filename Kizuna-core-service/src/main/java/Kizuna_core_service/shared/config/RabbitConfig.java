package Kizuna_core_service.shared.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "kizuna.exchange";

    @Bean
    public TopicExchange kizunaExchange() {
        return new TopicExchange(EXCHANGE);
    }
}
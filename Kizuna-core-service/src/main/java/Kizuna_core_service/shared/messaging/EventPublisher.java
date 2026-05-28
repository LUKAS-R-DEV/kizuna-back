package Kizuna_core_service.shared.messaging;

import Kizuna_core_service.shared.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(String topic,
                        String entity,
                        String entityId,
                        String userId,
                        String username,
                        Object data) {

        Map<String, Object> event = new HashMap<>();
        event.put("entity", entity);
        event.put("entityId", entityId);
        event.put("userId", userId);
        event.put("username", username);
        event.put("timestamp", Instant.now().toString());
        event.put("data", data);

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                topic,
                event
        );
    }
}
package kizuna_notification_service.notification.listener;

import kizuna_notification_service.notification.dto.GenericEventDto;
import kizuna_notification_service.notification.processor.NotificationProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationProcessor notificationProcessor;

    @RabbitListener(queues = "notification.queue")
    public void consume(GenericEventDto event) {
        notificationProcessor.process(event);
        System.out.println("[NOTIFICATION] Evento processado: " + event);
    }
}

package com.kizuna.data_service.consumer;

import com.kizuna.data_service.dto.GenericEventDto;
import com.kizuna.data_service.processor.EventProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventConsumer {
private final EventProcessor eventProcessor;

@RabbitListener(queues = "data.events.queue")
public void consume(GenericEventDto event) {
    eventProcessor.process(event);
    System.out.println("Evento processado: " + event);
}

}
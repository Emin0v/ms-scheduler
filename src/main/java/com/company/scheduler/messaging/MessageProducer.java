package com.company.scheduler.messaging;

import static com.company.scheduler.model.constant.ApplicationConstants.RabbitMqConstants.BINDING_NAME;
import static com.company.scheduler.model.constant.ApplicationConstants.RabbitMqConstants.ROUTING_KEY;

import com.company.scheduler.messaging.model.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageProducer {

    private final StreamBridge streamBridge;

    public void send(BaseEvent baseEvent) {
        final String eventId = baseEvent.getEventId();
        final String routingKey = baseEvent.getResponseQueueInfo().getRoutingKey();

        log.info("Publishing message with ID: {} to routing key: {}", eventId, routingKey);

        boolean sent = streamBridge.send(BINDING_NAME, buildMessage(baseEvent, routingKey));

        if (sent) {
            log.info("Message sent successfully. Event ID: {}, Routing key: {}", eventId, routingKey);
        } else {
            log.error("Failed to send message. Event ID: {}", eventId);
            throw new IllegalStateException("Message was not accepted for delivery: " + eventId);
        }
    }

    private Message<BaseEvent> buildMessage(BaseEvent baseEvent, String routingKey) {
        return MessageBuilder
                .withPayload(baseEvent)
                .setHeader(ROUTING_KEY, routingKey)
                .build();
    }
}

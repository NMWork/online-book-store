package com.microservices.onlinebookstore.notificationservice.messaging;

import com.microservices.onlinebookstore.notificationservice.event.OrderCreatedEvent;
import com.microservices.onlinebookstore.notificationservice.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private final NotificationService notificationService;
    public OrderEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "${spring.kafka.template.default-topic}",
            groupId = "notification-group"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        notificationService.sendOrderCreatedNotification(event);
    }


}

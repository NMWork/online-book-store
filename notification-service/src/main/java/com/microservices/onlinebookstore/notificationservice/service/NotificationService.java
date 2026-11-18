package com.microservices.onlinebookstore.notificationservice.service;

import com.microservices.onlinebookstore.notificationservice.event.OrderCreatedEvent;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void sendOrderCreatedNotification(OrderCreatedEvent event) {

        System.out.println("==================================");
        System.out.println("Sending Order Created Notification");
        System.out.println("Order ID: " + event.orderId());
        System.out.println("Customer ID: " + event.customerId());
        System.out.println("Order Amount: " + event.totalAmount());
        System.out.println("Order Status: " + event.status());
        System.out.println("==================================");
    }

}

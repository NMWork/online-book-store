package com.educba.onlinebookstore.orderservice.exception;

public class OrderNotFoundException extends RuntimeException{
    public OrderNotFoundException(Long id) {
        super("Order with id " + id + " not found");
    }
}

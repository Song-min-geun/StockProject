package com.example.stockservice.service;


import com.example.dto.OrderCancelledEvent;
import com.example.dto.OrderCreatedEvent;

public interface StockService {
    void decreaseStock(OrderCreatedEvent event);
    void increaseStock(OrderCancelledEvent event);
}
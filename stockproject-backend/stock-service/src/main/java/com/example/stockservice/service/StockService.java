package com.example.stockservice.service;

import com.example.stockservice.dto.OrderCreatedEvent;

public interface StockService {
    void decreaseStock(OrderCreatedEvent event);
}
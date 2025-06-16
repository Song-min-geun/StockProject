package com.example.stockservice.dto;

import java.time.LocalDateTime;
import java.util.List;

// Kafka로부터 수신할 이벤트 메시지의 구조
public record OrderCreatedEvent(
        String orderId,
        Long userId,
        Long totalPrice,
        LocalDateTime createdAt,
        List<OrderItemPayload> items
) {}
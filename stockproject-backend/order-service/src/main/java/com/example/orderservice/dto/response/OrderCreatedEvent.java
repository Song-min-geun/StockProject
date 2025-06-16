package com.example.orderservice.dto.response;

import com.example.orderservice.dto.request.OrderItemPayload;

import java.time.LocalDateTime;
import java.util.List;

// Kafka에 발행할 이벤트의 최종 형태
public record OrderCreatedEvent(
        String orderId,
        Long userId,
        Long totalPrice,
        LocalDateTime createdAt,
        List<OrderItemPayload> items
) {}

package com.example.dto;

import java.time.LocalDateTime;
import java.util.List;

// 주문 취소 시 Kafka에 발행할 'Fat Event'
public record OrderCancelledEvent(
        String orderId,
        Long userId,
        Long totalPrice,
        LocalDateTime createdAt,
        List<OrderItemPayload> items
) {}
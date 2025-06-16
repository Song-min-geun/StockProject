package com.example.orderservice.dto.response;

import java.time.LocalDateTime;

public record OrderResponseDto (
    String orderId,
    String status,
    Long totalPrice,
    LocalDateTime orderedAt
){}

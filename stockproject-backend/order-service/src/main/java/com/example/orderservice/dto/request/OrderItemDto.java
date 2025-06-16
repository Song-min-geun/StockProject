package com.example.orderservice.dto.request;

public record OrderItemDto(
    Long productId,
    Integer quantity
){}

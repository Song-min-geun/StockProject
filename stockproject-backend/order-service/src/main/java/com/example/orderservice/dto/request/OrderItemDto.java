package com.example.orderservice.dto.request;

public record OrderItemDto(
    String productId,
    Integer quantity
){}

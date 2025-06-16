package com.example.stockservice.dto;

public record OrderItemPayload(
        Long productId,
        Integer quantity,
        Long price
) {}
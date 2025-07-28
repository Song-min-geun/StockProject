package com.example.orderservice.dto.response;

public record ProductResponseDto(
        String productId,
        String name,
        Long price
) {}

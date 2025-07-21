package com.example.orderservice.dto.response;

public record ProductResponseDto(
        Long productId,
        String name,
        Long price
) {}

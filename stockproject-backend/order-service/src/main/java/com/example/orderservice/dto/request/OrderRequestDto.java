package com.example.orderservice.dto.request;


import java.util.List;

public record OrderRequestDto (
        Long userId,
        List<OrderItemDto> items
){}

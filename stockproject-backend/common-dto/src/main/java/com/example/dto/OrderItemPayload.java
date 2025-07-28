package com.example.dto;

// OrderCreatedEvent에 포함될 상품 정보
public record OrderItemPayload(
        String productId,
        Integer quantity,
        Long price // 주문 당시의 개별 단가
) {}

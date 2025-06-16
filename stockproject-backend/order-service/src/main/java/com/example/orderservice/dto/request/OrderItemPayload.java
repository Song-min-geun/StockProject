package com.example.orderservice.dto.request;

// OrderCreatedEvent에 포함될 상품 정보
public record OrderItemPayload(
        Long productId,
        Integer quantity,
        Long price // 주문 당시의 개별 단가
) {}

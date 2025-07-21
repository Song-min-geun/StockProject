package com.example.orderservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING("주문 대기"),
    PAID("결제 완료"),
    SHIPPED("배송 중"),
    COMPLETED("배송 완료"),
    CANCELED("주문 취소");

    private final String description;
}

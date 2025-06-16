// OrderService.java (수정된 전체 내용)
package com.example.orderservice.service;

import com.example.orderservice.dto.response.OrderDetailResponseDto;
import com.example.orderservice.dto.request.OrderRequestDto;
import com.example.orderservice.dto.response.OrderResponseDto;

public interface OrderService {
    //주문 생성
    OrderResponseDto createOrder(OrderRequestDto requestDto);

    // 주문 단건 조회
    OrderDetailResponseDto getOrderByOrderId(String orderId);
}
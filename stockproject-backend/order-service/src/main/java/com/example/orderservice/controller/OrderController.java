package com.example.orderservice.controller;

import com.example.orderservice.dto.response.OrderDetailResponseDto;
import com.example.orderservice.dto.request.OrderRequestDto;
import com.example.orderservice.dto.response.OrderResponseDto;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService; // final 키워드 추가

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto requestDto) {
        OrderResponseDto responseDto = orderService.createOrder(requestDto);

        // 표준적인 Location URI 생성 방식으로 변경
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{orderId}")
                .buildAndExpand(responseDto.orderId())
                .toUri();

        return ResponseEntity.created(location).body(responseDto);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponseDto> getOrder(@PathVariable("orderId") String orderId) {
        OrderDetailResponseDto orderDetails = orderService.getOrderByOrderId(orderId);
        return ResponseEntity.ok(orderDetails);
    }
}
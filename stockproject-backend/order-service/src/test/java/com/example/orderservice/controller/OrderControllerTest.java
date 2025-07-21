package com.example.orderservice.controller;

import com.example.orderservice.dto.request.OrderItemDto;
import com.example.orderservice.dto.request.OrderRequestDto;
import com.example.orderservice.dto.response.OrderDetailResponseDto;
import com.example.orderservice.dto.response.OrderResponseDto;
import com.example.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;


    @Test
    @DisplayName("POST /api/v1/orders 요청 시, 주문을 생성하고 201 Created를 반환한다.")
    @WithMockUser
    void createOrder_Success() throws Exception {
        // given
        OrderRequestDto requestDto = new OrderRequestDto(1L, List.of(new OrderItemDto(101L, 2)));
        String orderId = UUID.randomUUID().toString();
        OrderResponseDto responseDto = new OrderResponseDto(orderId, "PENDING", 20000L, LocalDateTime.now());

        // orderService.createOrder가 호출되면 위에서 만든 responseDto를 반환하도록 설정
        when(orderService.createOrder(any(OrderRequestDto.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) // HTTP 201 상태 확인
                .andExpect(header().string("Location", "http://localhost/api/v1/orders/" + orderId)) // Location 헤더 확인
                .andExpect(jsonPath("$.orderId").value(orderId)); // 응답 본문의 orderId 확인
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} 요청 시, 주문 상세 정보를 조회하고 200 OK를 반환한다.")
    @WithMockUser
    void getOrder_Success() throws Exception {
        // given
        String orderId = UUID.randomUUID().toString();
        // 주문 상세 정보를 담은 DTO 생성
        OrderDetailResponseDto responseDto = new OrderDetailResponseDto(
                orderId,
                1L,
                "PENDING",
                20000L,
                LocalDateTime.now(),
                List.of(new OrderDetailResponseDto.OrderItemInfo(101L, 2, 10000L))
        );

        // orderService.getOrderByOrderId가 호출되면 위에서 만든 responseDto를 반환하도록 설정
        when(orderService.getOrderByOrderId(orderId)).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                        .with(csrf()))
                .andExpect(status().isOk()) // HTTP 200 상태 확인
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.items.length()").value(1)) // 주문 항목이 1개인지 확인
                .andExpect(jsonPath("$.items[0].productId").value(101L));
    }

    @Test
    @DisplayName("DELETE /api/v1/orders/{orderId} 요청 시, 주문 취소 로직을 호출하고 204 No Content를 반환한다.")
    @WithMockUser
    void cancelOrder_Success() throws Exception {
        // given
        String orderId = "test-order-id-123";
        doNothing().when(orderService).cancelOrder(orderId);

        // when & then
        mockMvc.perform(delete("/api/v1/orders/{orderId}", orderId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(orderService).cancelOrder(orderId);
    }
}
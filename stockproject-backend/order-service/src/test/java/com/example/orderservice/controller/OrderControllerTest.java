package com.example.orderservice.controller;

import com.example.orderservice.service.OrderService;
import com.example.userservice.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class) // OrderController를 테스트 대상으로 지정
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc; // API 요청을 시뮬레이션하기 위한 객체

    @MockitoBean // 가짜 OrderService를 Spring 컨테이너에 등록
    private OrderService orderService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("DELETE /api/v1/orders/{orderId} 요청 시, 주문 취소 로직을 호출하고 204 No Content를 반환한다.")
    @WithMockUser
    void cancelOrder_Success() throws Exception {
        // given
        String orderId = "test-order-id-123";
        // orderService.cancelOrder(orderId)가 호출되어도 아무 일도 일어나지 않도록 설정
        doNothing().when(orderService).cancelOrder(orderId);

        // when & then
        mockMvc.perform(delete("/api/v1/orders/" + orderId)
                        .with(csrf())) // 2. 여기에 csrf() 처리 추가
                .andExpect(status().isNoContent());

        // verify: orderService의 cancelOrder 메소드가 정확히 1번 호출되었는지 검증
        verify(orderService).cancelOrder(orderId);
    }
}
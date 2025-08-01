package com.example.stockservice.consumer;

import com.example.dto.OrderCancelledEvent;
import com.example.dto.OrderCreatedEvent;
import com.example.dto.OrderItemPayload;
import com.example.stockservice.service.StockService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderConsumerTest {

    @InjectMocks
    private OrderConsumer orderConsumer;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private StockService stockService;

    @Test
    @DisplayName("주문 생성 이벤트: 성공 - 메시지를 성공적으로 역직렬화하고 재고 차감 로직을 호출한다.")
    void listenOrderCreation_Success() throws JsonProcessingException {
        // given
        String orderId = "order-id-101";
        String productId = "product-id-101";
        int quantity = 2;
        long price = 10000L;
        
        OrderItemPayload orderItem = new OrderItemPayload(productId, quantity, price);
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId, 
                1L, // userId
                quantity * price, // totalPrice
                LocalDateTime.now(), 
                List.of(orderItem)
        );
        
        String message = "{\"orderId\":\"order-id-101\",\"userId\":1,\"totalPrice\":20000,\"createdAt\":\"2023-01-01T12:00:00\",\"items\":[{\"productId\":\"product-id-101\",\"quantity\":2,\"price\":10000}]}";
        
        when(objectMapper.readValue(message, OrderCreatedEvent.class)).thenReturn(event);
        doNothing().when(stockService).decreaseStock(any(OrderCreatedEvent.class));
        
        // when
        orderConsumer.listenOrderCreation(message);
        
        // then
        verify(objectMapper).readValue(message, OrderCreatedEvent.class);
        verify(stockService).decreaseStock(event);
    }

    @Test
    @DisplayName("주문 생성 이벤트: 실패 - 메시지 역직렬화에 실패하면 예외를 처리한다.")
    void listenOrderCreation_Fail_JsonProcessingException() throws JsonProcessingException {
        // given
        String invalidMessage = "invalid-json-message";
        
        when(objectMapper.readValue(invalidMessage, OrderCreatedEvent.class))
                .thenThrow(new JsonProcessingException("JSON 파싱 에러") {});
        
        // when
        orderConsumer.listenOrderCreation(invalidMessage);
        
        // then
        verify(objectMapper).readValue(invalidMessage, OrderCreatedEvent.class);
        verify(stockService, never()).decreaseStock(any(OrderCreatedEvent.class));
    }

    @Test
    @DisplayName("주문 생성 이벤트: 실패 - 재고 차감 로직에서 예외가 발생하면 처리한다.")
    void listenOrderCreation_Fail_StockServiceException() throws JsonProcessingException {
        // given
        String orderId = "order-id-101";
        String productId = "product-id-101";
        int quantity = 2;
        long price = 10000L;
        
        OrderItemPayload orderItem = new OrderItemPayload(productId, quantity, price);
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId, 
                1L, // userId
                quantity * price, // totalPrice
                LocalDateTime.now(), 
                List.of(orderItem)
        );
        
        String message = "{\"orderId\":\"order-id-101\",\"userId\":1,\"totalPrice\":20000,\"createdAt\":\"2023-01-01T12:00:00\",\"items\":[{\"productId\":\"product-id-101\",\"quantity\":2,\"price\":10000}]}";
        
        when(objectMapper.readValue(message, OrderCreatedEvent.class)).thenReturn(event);
        doThrow(new IllegalArgumentException("상품에 대한 재고 정보가 없습니다."))
                .when(stockService).decreaseStock(any(OrderCreatedEvent.class));
        
        // when
        orderConsumer.listenOrderCreation(message);
        
        // then
        verify(objectMapper).readValue(message, OrderCreatedEvent.class);
        verify(stockService).decreaseStock(event);
    }

    @Test
    @DisplayName("주문 취소 이벤트: 성공 - 메시지를 성공적으로 역직렬화하고 재고 복구 로직을 호출한다.")
    void listenOrderDeletion_Success() throws JsonProcessingException {
        // given
        String orderId = "order-id-101";
        String productId = "product-id-101";
        int quantity = 2;
        long price = 10000L;
        
        OrderItemPayload orderItem = new OrderItemPayload(productId, quantity, price);
        OrderCancelledEvent event = new OrderCancelledEvent(
                orderId, 
                1L, // userId
                quantity * price, // totalPrice
                LocalDateTime.now(), 
                List.of(orderItem)
        );
        
        String message = "{\"orderId\":\"order-id-101\",\"userId\":1,\"totalPrice\":20000,\"createdAt\":\"2023-01-01T12:00:00\",\"items\":[{\"productId\":\"product-id-101\",\"quantity\":2,\"price\":10000}]}";
        
        when(objectMapper.readValue(message, OrderCancelledEvent.class)).thenReturn(event);
        doNothing().when(stockService).increaseStock(any(OrderCancelledEvent.class));
        
        // when
        orderConsumer.listenOrderDeletion(message);
        
        // then
        verify(objectMapper).readValue(message, OrderCancelledEvent.class);
        verify(stockService).increaseStock(event);
    }

    @Test
    @DisplayName("주문 취소 이벤트: 실패 - 메시지 역직렬화에 실패하면 예외를 처리한다.")
    void listenOrderDeletion_Fail_JsonProcessingException() throws JsonProcessingException {
        // given
        String invalidMessage = "invalid-json-message";
        
        when(objectMapper.readValue(invalidMessage, OrderCancelledEvent.class))
                .thenThrow(new JsonProcessingException("JSON 파싱 에러") {});
        
        // when
        orderConsumer.listenOrderDeletion(invalidMessage);
        
        // then
        verify(objectMapper).readValue(invalidMessage, OrderCancelledEvent.class);
        verify(stockService, never()).increaseStock(any(OrderCancelledEvent.class));
    }

    @Test
    @DisplayName("주문 취소 이벤트: 실패 - 재고 복구 로직에서 예외가 발생하면 처리한다.")
    void listenOrderDeletion_Fail_StockServiceException() throws JsonProcessingException {
        // given
        String orderId = "order-id-101";
        String productId = "product-id-101";
        int quantity = 2;
        long price = 10000L;
        
        OrderItemPayload orderItem = new OrderItemPayload(productId, quantity, price);
        OrderCancelledEvent event = new OrderCancelledEvent(
                orderId, 
                1L, // userId
                quantity * price, // totalPrice
                LocalDateTime.now(), 
                List.of(orderItem)
        );
        
        String message = "{\"orderId\":\"order-id-101\",\"userId\":1,\"totalPrice\":20000,\"createdAt\":\"2023-01-01T12:00:00\",\"items\":[{\"productId\":\"product-id-101\",\"quantity\":2,\"price\":10000}]}";
        
        when(objectMapper.readValue(message, OrderCancelledEvent.class)).thenReturn(event);
        doThrow(new IllegalArgumentException("상품에 대한 재고 정보가 없습니다."))
                .when(stockService).increaseStock(any(OrderCancelledEvent.class));
        
        // when
        orderConsumer.listenOrderDeletion(message);
        
        // then
        verify(objectMapper).readValue(message, OrderCancelledEvent.class);
        verify(stockService).increaseStock(event);
    }
}
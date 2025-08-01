package com.example.stockservice.service;

import com.example.dto.OrderCancelledEvent;
import com.example.dto.OrderCreatedEvent;
import com.example.dto.OrderItemPayload;
import com.example.stockservice.domain.Stock;
import com.example.stockservice.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @InjectMocks
    private StockServiceImpl stockService;

    @Mock
    private StockRepository stockRepository;

    @Test
    @DisplayName("재고 차감: 성공 - 주문 생성 이벤트를 받아 재고를 성공적으로 차감한다.")
    void decreaseStock_Success() {
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
        
        Stock stock = new Stock(productId, 10L);
        when(stockRepository.findById(productId)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        
        // when
        stockService.decreaseStock(event);
        
        // then
        assertThat(stock.getQuantity()).isEqualTo(8L); // 10 - 2 = 8
        verify(stockRepository).save(stock);
    }

    @Test
    @DisplayName("재고 차감: 실패 - 상품에 대한 재고 정보가 없을 경우 예외가 발생한다.")
    void decreaseStock_Fail_StockNotFound() {
        // given
        String orderId = "order-id-101";
        String productId = "non-existing-product-id";
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
        
        when(stockRepository.findById(productId)).thenReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> stockService.decreaseStock(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품에 대한 재고 정보가 없습니다.");
        
        verify(stockRepository, never()).save(any(Stock.class));
    }

    @Test
    @DisplayName("재고 차감: 실패 - 재고가 부족할 경우 예외가 발생한다.")
    void decreaseStock_Fail_InsufficientStock() {
        // given
        String orderId = "order-id-101";
        String productId = "product-id-101";
        int quantity = 20; // 재고보다 많은 수량
        long price = 10000L;
        
        OrderItemPayload orderItem = new OrderItemPayload(productId, quantity, price);
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId, 
                1L, // userId
                quantity * price, // totalPrice
                LocalDateTime.now(), 
                List.of(orderItem)
        );
        
        Stock stock = new Stock(productId, 10L); // 재고는 10개
        when(stockRepository.findById(productId)).thenReturn(Optional.of(stock));
        
        // when & then
        assertThatThrownBy(() -> stockService.decreaseStock(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재고가 부족합니다");
        
        verify(stockRepository, never()).save(any(Stock.class));
    }

    @Test
    @DisplayName("재고 복구: 성공 - 주문 취소 이벤트를 받아 재고를 성공적으로 복구한다.")
    void increaseStock_Success() {
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
        
        Stock stock = new Stock(productId, 8L);
        when(stockRepository.findById(productId)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        
        // when
        stockService.increaseStock(event);
        
        // then
        assertThat(stock.getQuantity()).isEqualTo(10L); // 8 + 2 = 10
        verify(stockRepository).save(stock);
    }

    @Test
    @DisplayName("재고 복구: 실패 - 상품에 대한 재고 정보가 없을 경우 예외가 발생한다.")
    void increaseStock_Fail_StockNotFound() {
        // given
        String orderId = "order-id-101";
        String productId = "non-existing-product-id";
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
        
        when(stockRepository.findById(productId)).thenReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> stockService.increaseStock(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품에 대한 재고 정보가 없습니다.");
        
        verify(stockRepository, never()).save(any(Stock.class));
    }

    @Test
    @DisplayName("재고 차감 및 복구: 성공 - 여러 상품의 재고를 처리한다.")
    void processMultipleItems_Success() {
        // given
        String orderId = "order-id-101";
        String productId1 = "product-id-101";
        String productId2 = "product-id-102";
        int quantity1 = 2;
        int quantity2 = 3;
        long price1 = 10000L;
        long price2 = 20000L;
        
        OrderItemPayload orderItem1 = new OrderItemPayload(productId1, quantity1, price1);
        OrderItemPayload orderItem2 = new OrderItemPayload(productId2, quantity2, price2);
        
        OrderCreatedEvent createEvent = new OrderCreatedEvent(
                orderId, 
                1L, // userId
                (quantity1 * price1) + (quantity2 * price2), // totalPrice
                LocalDateTime.now(), 
                List.of(orderItem1, orderItem2)
        );
        
        OrderCancelledEvent cancelEvent = new OrderCancelledEvent(
                orderId, 
                1L, // userId
                (quantity1 * price1) + (quantity2 * price2), // totalPrice
                LocalDateTime.now(), 
                List.of(orderItem1, orderItem2)
        );
        
        Stock stock1 = new Stock(productId1, 10L);
        Stock stock2 = new Stock(productId2, 15L);
        
        when(stockRepository.findById(productId1)).thenReturn(Optional.of(stock1));
        when(stockRepository.findById(productId2)).thenReturn(Optional.of(stock2));
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // when - decrease
        stockService.decreaseStock(createEvent);
        
        // then - decrease
        assertThat(stock1.getQuantity()).isEqualTo(8L); // 10 - 2 = 8
        assertThat(stock2.getQuantity()).isEqualTo(12L); // 15 - 3 = 12
        verify(stockRepository, times(2)).save(any(Stock.class));
        
        // when - increase
        stockService.increaseStock(cancelEvent);
        
        // then - increase
        assertThat(stock1.getQuantity()).isEqualTo(10L); // 8 + 2 = 10
        assertThat(stock2.getQuantity()).isEqualTo(15L); // 12 + 3 = 15
        verify(stockRepository, times(4)).save(any(Stock.class));
    }
}
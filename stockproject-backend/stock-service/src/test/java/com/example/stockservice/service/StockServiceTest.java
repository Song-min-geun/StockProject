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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockService 비즈니스 로직 테스트")
class StockServiceTest {

    @InjectMocks
    private StockServiceImpl stockService; // 테스트 대상 클래스

    @Mock
    private StockRepository stockRepository; // 가짜 StockRepository

    @Test
    @DisplayName("재고 감소: 성공 - 주문 생성 이벤트에 따라 재고가 감소한다")
    void decreaseStock_Success() {
        // given
        long productId = 101L;
        int quantityToDecrease = 10;
        Stock stock = new Stock(productId, 100L); // 기존 재고: 100

        // 주문 생성 이벤트 DTO 생성
        OrderCreatedEvent event = new OrderCreatedEvent(
                "order-id-123",
                1L,
                10000L,
                LocalDateTime.now(),
                List.of(new OrderItemPayload(productId, quantityToDecrease, 1000L))
        );

        // stockRepository가 productId로 Stock을 찾으면 위에서 만든 stock 객체를 반환하도록 설정
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));

        // when
        stockService.decreaseStock(event);

        // then
        // 재고가 요청된 수량만큼 감소했는지 확인
        assertThat(stock.getQuantity()).isEqualTo(90L);
    }

    @Test
    @DisplayName("재고 감소: 실패 - 재고 정보를 찾을 수 없을 때 예외가 발생한다")
    void decreaseStock_Fail_StockNotFound() {
        // given
        long productId = 999L; // 존재하지 않는 상품 ID
        OrderCreatedEvent event = new OrderCreatedEvent(
                "order-id-123",
                1L,
                10000L,
                LocalDateTime.now(),
                List.of(new OrderItemPayload(productId, 10, 1000L))
        );

        // 재고 정보가 없다고 설정
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.empty());

        // when & then
        // decreaseStock 호출 시 예외가 발생하는지 검증
        assertThatThrownBy(() -> stockService.decreaseStock(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품에 대한 재고 정보가 없습니다.");
    }

    @Test
    @DisplayName("재고 증가: 성공 - 주문 취소 이벤트에 따라 재고가 복구된다")
    void increaseStock_Success() {
        // given
        long productId = 101L;
        int quantityToIncrease = 10;
        Stock stock = new Stock(productId, 50L); // 기존 재고: 50

        // 주문 취소 이벤트 DTO 생성
        OrderCancelledEvent event = new OrderCancelledEvent(
                "order-id-123",
                1L,
                10000L,
                LocalDateTime.now(),
                List.of(new OrderItemPayload(productId, quantityToIncrease, 1000L))
        );

        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));

        // when
        stockService.increaseStock(event);

        // then
        // 재고가 요청된 수량만큼 증가했는지 확인
        assertThat(stock.getQuantity()).isEqualTo(60L);
    }
}
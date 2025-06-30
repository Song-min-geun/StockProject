package com.example.stockservice.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class StockTest {

    @Test
    @DisplayName("재고가 충분할 경우, 요청된 수량만큼 재고가 성공적으로 감소한다.")
    void decrease_Success() {
        // given
        Stock stock = new Stock(1L, 100L); // productId 1, quantity 100

        // when
        stock.decrease(10L);

        // then
        assertThat(stock.getQuantity()).isEqualTo(90L);
    }

    @Test
    @DisplayName("재고보다 많은 수량을 요청할 경우, 재고 부족 예외가 발생한다.")
    void decrease_ThrowsException_WhenStockIsInsufficient() {
        // given
        Stock stock = new Stock(1L, 5L); // productId 1, quantity 5

        // when & then
        assertThatThrownBy(() -> stock.decrease(10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("재고가 부족합니다.");
    }
}
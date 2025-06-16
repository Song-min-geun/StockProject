package com.example.stockservice.service;

import com.example.stockservice.domain.Stock;
import com.example.stockservice.dto.OrderCreatedEvent;
import com.example.stockservice.dto.OrderItemPayload;
import com.example.stockservice.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;

    @Override
    @Transactional
    public void decreaseStock(OrderCreatedEvent event) {
        log.info("재고 차감 로직을 실행합니다. OrderId: {}", event.orderId());

        for (OrderItemPayload item : event.items()) {
            // 1. PESSIMISTIC_WRITE 락을 걸고 재고 정보를 조회합니다.
            Stock stock = stockRepository.findByProductId(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException("상품에 대한 재고 정보가 없습니다."));

            // 2. Entity 내부의 비즈니스 로직을 통해 재고를 차감합니다.
            log.info("기존 재고: {}, 요청 수량: {}", stock.getQuantity(), item.quantity());
            stock.decrease(item.quantity().longValue());
            log.info("차감 후 재고: {}", stock.getQuantity());

            // 3. @Transactional 어노테이션에 의해 메소드가 종료될 때,
            // 변경된 Stock 엔티티가 자동으로 DB에 UPDATE 됩니다 (Dirty Checking).
            // stockRepository.save(stock)를 명시적으로 호출할 필요가 없습니다.
        }
    }
}
package com.example.stockservice.service;


import com.example.dto.OrderCancelledEvent;
import com.example.dto.OrderItemPayload;
import com.example.stockservice.domain.Stock;
import com.example.dto.OrderCreatedEvent;
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
    @Transactional // Spring Data Redis도 트랜잭션을 지원합니다.
    public void decreaseStock(OrderCreatedEvent event) {
        log.info("재고 차감 로직을 실행합니다. OrderId: {}", event.orderId());

        for (OrderItemPayload item : event.items()) {
            // ⭐️ JpaRepository의 findByProductId 대신, CrudRepository의 findById를 사용합니다.
            Stock stock = stockRepository.findById(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException("상품에 대한 재고 정보가 없습니다."));

            log.info("기존 재고: {}, 요청 수량: {}", stock.getQuantity(), item.quantity());
            stock.decrease(item.quantity().longValue());
            log.info("차감 후 재고: {}", stock.getQuantity());

            // ⭐️ 변경된 재고 정보를 Redis에 다시 저장(덮어쓰기)합니다.
            stockRepository.save(stock);
        }
    }

    @Override
    @Transactional
    public void increaseStock(OrderCancelledEvent event) {
        log.info("재고 복구 로직 실행합니다. orderId : {}", event.orderId());

        for (OrderItemPayload item : event.items()) {
            Stock stock = stockRepository.findById(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException("상품에 대한 재고 정보가 없습니다."));
            stock.increase(item.quantity().longValue());
            stockRepository.save(stock);
        }
    }
}
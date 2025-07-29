package com.example.stockservice.consumer;

import com.example.dto.ProductCreatedEvent;
import com.example.stockservice.domain.Stock;
import com.example.stockservice.repository.StockRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductConsumer {

    private final ObjectMapper objectMapper;
    private final StockRepository stockRepository;

    @KafkaListener(topics = "product-created", groupId = "stock-service-group")
    public void listenProductCreation(String message) {
        log.info("Received product creation message: {}", message);

        try {
            ProductCreatedEvent event = objectMapper.readValue(message, ProductCreatedEvent.class);

            // Redis에 초기 재고 정보 생성
            Stock stock = new Stock(event.productId(), (long) event.initialStock());
            stockRepository.save(stock);
            log.info("초기 재고 정보를 Redis에 저장했습니다. ProductId: {}", event.productId());

        } catch (JsonProcessingException e) {
            log.error("메시지 역직렬화에 실패했습니다: {}", message, e);
        } catch (Exception e) {
            log.error("메시지 처리에 실패했습니다: {}", message, e);
        }
    }

    @KafkaListener(topics = "product-deleted", groupId = "stock-service-group")
    public void listenProductDeletion(String productId) {
        log.info("Received product deletion message for productId: {}", productId);
        try {
            // Redis에서 해당 상품 ID의 재고 정보 삭제
            stockRepository.deleteById(productId);
            log.info("재고 정보를 Redis에서 삭제했습니다. ProductId: {}", productId);
        } catch (Exception e) {
            log.error("재고 정보 삭제에 실패했습니다. ProductId: {}", productId, e);
        }
    }
}
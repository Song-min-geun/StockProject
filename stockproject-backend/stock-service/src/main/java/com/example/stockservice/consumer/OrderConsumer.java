package com.example.stockservice.consumer;

import com.example.stockservice.dto.OrderCreatedEvent;
import com.example.stockservice.service.StockService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final ObjectMapper objectMapper;
    private final StockService stockService;

    @KafkaListener(topics = "order-created", groupId = "stock-service-group")
    public void listen(String message) {
        log.info("Received message: {}", message);

        try {
            // 1. 수신한 JSON 메시지를 OrderCreatedEvent 객체로 변환 (역직렬화)
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);

            // 2. 재고 차감 로직이 담긴 서비스 메소드 호출
            stockService.decreaseStock(event);

        } catch (JsonProcessingException e) {
            log.error("메시지 역직렬화에 실패했습니다: {}", message, e);
        } catch (Exception e) {
            log.error("메시지 처리에 실패했습니다: {}", message, e);
            // 에러 처리 로직 (예: 재시도, 에러 로깅 등)
        }
    }
}
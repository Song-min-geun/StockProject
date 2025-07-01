package com.example.stockservice.consumer;

import com.example.dto.OrderCancelledEvent;
import com.example.stockservice.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCancellationConsumer {

    private final ObjectMapper objectMapper;
    private final StockService stockService;

    @KafkaListener(topics = "order-cancelled", groupId = "stock-service-group")
    public void listen(String message) {
        log.info("Received order cancellation message: {}", message);
        try {
            // 수정된 DTO로 메시지 역직렬화
            OrderCancelledEvent event = objectMapper.readValue(message, OrderCancelledEvent.class);
            stockService.increaseStock(event);
        } catch (Exception e) {
            log.error("메시지 처리 실패 (주문 취소): {}", message, e);
        }
    }
}

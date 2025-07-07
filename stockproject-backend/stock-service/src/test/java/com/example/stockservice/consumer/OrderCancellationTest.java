package com.example.stockservice.consumer;

import com.example.dto.OrderCancelledEvent;
import com.example.dto.OrderItemPayload;
import com.example.stockservice.service.StockService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@DisplayName("OrderCancellationConsumer 통합 테스트")
class OrderCancellationConsumerTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StockService stockService;

    private final String topic = "order-cancelled";

    @Test
    @DisplayName("주문 취소 이벤트 메시지 수신 시, StockService의 increaseStock 메소드를 호출한다")
    void listen_OrderCancelledEvent_Success() throws JsonProcessingException {
        // given
        // Kafka로 보낼 주문 취소 이벤트 객체 생성
        OrderCancelledEvent event = new OrderCancelledEvent(
                "order-id-to-cancel-456",
                1L,
                20000L,
                LocalDateTime.now(),
                List.of(new OrderItemPayload(101L, 2, 10000L))
        );
        String message = objectMapper.writeValueAsString(event);

        // when
        // 생성한 메시지를 "order-cancelled" 토픽으로 발행
        kafkaTemplate.send(topic, message);

        // then
        // 2초 안에 stockService.increaseStock 메소드가 1번 호출되는지 확인
        verify(stockService, timeout(2000).times(1))
                .increaseStock(any(OrderCancelledEvent.class));
    }
}
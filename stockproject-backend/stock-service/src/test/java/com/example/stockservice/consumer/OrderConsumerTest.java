package com.example.stockservice.consumer;

import com.example.dto.OrderCreatedEvent;
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

@SpringBootTest // 통합 테스트를 위해 전체 Spring 컨텍스트를 로드
@DirtiesContext // 테스트 간 컨텍스트 오염을 방지하기 위해 테스트 종료 후 컨텍스트를 초기화
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" }) // 내장 Kafka 서버 실행
@DisplayName("OrderConsumer 통합 테스트")
class OrderConsumerTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate; // 테스트에서 메시지를 보낼 때 사용

    @Autowired
    private ObjectMapper objectMapper; // 객체를 JSON으로 변환하기 위해 주입

    @MockitoBean // 실제 StockService 대신 가짜 객체를 주입
    private StockService stockService;

    private final String topic = "order-created";

    @Test
    @DisplayName("주문 생성 이벤트 메시지 수신 시, StockService의 decreaseStock 메소드를 호출한다")
    void listen_OrderCreatedEvent_Success() throws JsonProcessingException, InterruptedException {
        // given
        // Kafka로 보낼 이벤트 객체 생성
        OrderCreatedEvent event = new OrderCreatedEvent(
                "order-id-test-123",
                1L,
                20000L,
                LocalDateTime.now(),
                List.of(new OrderItemPayload(101L, 2, 10000L))
        );
        String message = objectMapper.writeValueAsString(event);

        // when
        // 생성한 메시지를 "order-created" 토픽으로 발행
        kafkaTemplate.send(topic, message);

        // then
        // 비동기적으로 처리되는 로직을 검증하기 위해,
        // 1초 안에 stockService.decreaseStock 메소드가 1번 호출되는지 확인
        // Consumer가 메시지를 처리할 시간을 벌어주기 위함
        verify(stockService, timeout(1000).times(1))
                .decreaseStock(any(OrderCreatedEvent.class));
    }
}
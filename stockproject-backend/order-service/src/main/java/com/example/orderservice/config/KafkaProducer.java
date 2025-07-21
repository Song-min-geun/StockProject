package com.example.orderservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // 객체를 JSON 문자열로 변환하여 지정된 토픽으로 메시지를 보냅니다.
    public void send(String topic, Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, jsonPayload);
            log.info("Sent payload={} to topic={}", jsonPayload, topic);
        } catch (JsonProcessingException e) {
            log.error("Error converting payload to JSON", e);
            // 예외 처리 전략 필요
        }
    }
}

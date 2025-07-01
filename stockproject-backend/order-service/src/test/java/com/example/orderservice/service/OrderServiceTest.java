package com.example.orderservice.service;

import com.example.dto.OrderCancelledEvent;
import com.example.dto.OrderCreatedEvent;
import com.example.orderservice.config.KafkaProducer;
import com.example.orderservice.domain.Order;
import com.example.orderservice.domain.OrderItem;
import com.example.orderservice.domain.OrderStatus;
import com.example.orderservice.dto.request.OrderItemDto;
import com.example.orderservice.dto.request.OrderRequestDto;
import com.example.orderservice.dto.response.OrderResponseDto;
import com.example.orderservice.dto.response.ProductResponseDto;
import com.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WebClient webClient;

    @Mock
    private KafkaProducer kafkaProducer;

    // WebClient의 복잡한 체이닝을 모킹하기 위한 설정
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Test
    @DisplayName("정상적인 주문 요청이 오면, 주문을 성공적으로 생성하고 Kafka 이벤트를 발행한다.")
    void createOrder_Success() {
        // given
        long userId = 1L;
        long productId = 101L;
        int quantity = 2;
        long price = 10000L;

        OrderRequestDto requestDto = new OrderRequestDto(userId, List.of(new OrderItemDto(productId, quantity)));

        // Mocking: product-service API 호출 결과 설정
        ProductResponseDto productResponse = new ProductResponseDto(productId, "테스트 상품", price);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductResponseDto[].class)).thenReturn(Mono.just(new ProductResponseDto[]{productResponse}));

        // Mocking: orderRepository.save()가 호출되면 인자 그대로 반환하도록 설정
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        OrderResponseDto responseDto = orderService.createOrder(requestDto);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.totalPrice()).isEqualTo(price * quantity);
        assertThat(responseDto.status()).isEqualTo("PENDING");

        // verify: KafkaProducer의 send 메소드가 1번 호출되었는지 검증
        verify(kafkaProducer, times(1)).send(eq("order-created"), any(OrderCreatedEvent.class));
    }

    @Test
    @DisplayName("주문 취소 요청이 오면, 주문 상태를 변경하고 Kafka로 취소 이벤트 발행한다.")
    void cancelOrder_Success(){
        //given
        String orderId = UUID.randomUUID().toString();
        Order order = Order.createOrder(orderId,1L,List.of(new OrderItem(101L, 10000L,2)));
        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));

        //when
        orderService.cancelOrder(orderId);

        //then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        verify(kafkaProducer, times(1)).send(eq("order-cancelled"), any(OrderCancelledEvent.class));
    }
}
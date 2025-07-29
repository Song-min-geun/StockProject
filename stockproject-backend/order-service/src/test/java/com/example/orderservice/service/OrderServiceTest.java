//package com.example.orderservice.service;
//
//import com.example.dto.OrderCancelledEvent;
//import com.example.dto.OrderCreatedEvent;
//import com.example.orderservice.config.KafkaProducer;
//import com.example.orderservice.domain.Order;
//import com.example.orderservice.domain.OrderItem;
//import com.example.orderservice.domain.OrderStatus;
//import com.example.orderservice.dto.request.OrderItemDto;
//import com.example.orderservice.dto.request.OrderRequestDto;
//import com.example.orderservice.dto.response.OrderDetailResponseDto;
//import com.example.orderservice.dto.response.OrderResponseDto;
//import com.example.orderservice.dto.response.ProductResponseDto;
//import com.example.orderservice.repository.OrderRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class OrderServiceTest {
//
//    @InjectMocks
//    private OrderServiceImpl orderService;
//
//    @Mock
//    private OrderRepository orderRepository;
//
//    @Mock
//    private WebClient webClient;
//
//    @Mock
//    private KafkaProducer kafkaProducer;
//
//    // WebClient의 복잡한 체이닝을 모킹하기 위한 설정
//    @Mock
//    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
//    @Mock
//    private WebClient.RequestHeadersSpec requestHeadersSpec;
//    @Mock
//    private WebClient.ResponseSpec responseSpec;
//
//    @Test
//    @DisplayName("주문 조회: 성공 - orderId로 주문 상세 정보를 성공적으로 조회한다.")
//    void getOrderByOrderId_Success() {
//        // given
//        String orderId = UUID.randomUUID().toString();
//        String productId = UUID.randomUUID().toString();
//        Order order = Order.createOrder(orderId, 1L, List.of(new OrderItem(productId, 10000L, 2)));
//        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));
//
//        // when
//        OrderDetailResponseDto responseDto = orderService.getOrderByOrderId(orderId);
//
//        // then
//        assertThat(responseDto).isNotNull();
//        assertThat(responseDto.orderId()).isEqualTo(orderId);
//        assertThat(responseDto.items().get(0).productId()).isEqualTo(101L);
//    }
//
//    @Test
//    @DisplayName("주문 조회: 실패 - 존재하지 않는 orderId로 조회 시 예외가 발생한다.")
//    void getOrderByOrderId_Fail_OrderNotFound() {
//        // given
//        String nonExistingOrderId = "non-existing-order-id";
//        when(orderRepository.findByOrderId(nonExistingOrderId)).thenReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> orderService.getOrderByOrderId(nonExistingOrderId))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage("주문을 찾을 수 없습니다.");
//    }
//
//    @Test
//    @DisplayName("주문 생성: 실패 - 상품 정보를 찾을 수 없을 때 예외가 발생한다.")
//    void createOrder_Fail_ProductNotFound() {
//        // given
//        long nonExistingProductId = 999L;
//        OrderRequestDto requestDto = new OrderRequestDto(1L, List.of(new OrderItemDto(nonExistingProductId, 1)));
//
//        // Mocking: product-service API 호출 시 빈 배열을 반환하도록 설정
//        when(webClient.get()).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
//        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
//        // 상품 정보가 없으므로 빈 배열을 Mono로 감싸서 반환
//        when(responseSpec.bodyToMono(ProductResponseDto[].class)).thenReturn(Mono.just(new ProductResponseDto[]{}));
//
//        // when & then
//        assertThatThrownBy(() -> orderService.createOrder(requestDto))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage("상품 정보를 찾을 수 없습니다: " + nonExistingProductId);
//    }
//
//    @Test
//    @DisplayName("정상적인 주문 요청이 오면, 주문을 성공적으로 생성하고 Kafka 이벤트를 발행한다.")
//    void createOrder_Success() {
//        // given
//        long userId = 1L;
//        long productId = 101L;
//        int quantity = 2;
//        long price = 10000L;
//
//        OrderRequestDto requestDto = new OrderRequestDto(userId, List.of(new OrderItemDto(productId, quantity)));
//
//        ProductResponseDto productResponse = new ProductResponseDto(productId, "테스트 상품", price);
//        when(webClient.get()).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
//        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
//        when(responseSpec.bodyToMono(ProductResponseDto[].class)).thenReturn(Mono.just(new ProductResponseDto[]{productResponse}));
//
//        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // when
//        OrderResponseDto responseDto = orderService.createOrder(requestDto);
//
//        // then
//        assertThat(responseDto).isNotNull();
//        assertThat(responseDto.totalPrice()).isEqualTo(price * quantity);
//        assertThat(responseDto.status()).isEqualTo("PENDING");
//
//        verify(kafkaProducer, times(1)).send(eq("order-created"), any(OrderCreatedEvent.class));
//    }
//
//    @Test
//    @DisplayName("주문 취소 요청이 오면, 주문 상태를 변경하고 Kafka로 취소 이벤트 발행한다.")
//    void cancelOrder_Success(){
//        //given
//        String orderId = UUID.randomUUID().toString();
//        String productId = UUID.randomUUID().toString();
//        Order order = Order.createOrder(orderId,1L,List.of(new OrderItem(productId, 10000L,2)));
//        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));
//
//        //when
//        orderService.cancelOrder(orderId);
//
//        //then
//        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
//        verify(kafkaProducer, times(1)).send(eq("order-cancelled"), any(OrderCancelledEvent.class));
//    }
//}
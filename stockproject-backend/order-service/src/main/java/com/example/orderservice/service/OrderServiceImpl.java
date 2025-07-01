package com.example.orderservice.service;

import com.example.dto.OrderCancelledEvent;
import com.example.dto.OrderCreatedEvent;
import com.example.dto.OrderItemPayload;
import com.example.orderservice.config.KafkaProducer;
import com.example.orderservice.domain.Order;
import com.example.orderservice.domain.OrderItem;
import com.example.orderservice.domain.OrderStatus;
import com.example.orderservice.dto.response.*;
import com.example.orderservice.dto.request.OrderItemDto;
import com.example.orderservice.dto.request.OrderRequestDto;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;
    private final KafkaProducer kafkaProducer;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto) { //userId, Items
        // 1. 요청된 상품 ID 목록 추출
        List<Long> productIds = requestDto.items().stream()
                .map(OrderItemDto::productId)
                .toList();

        // 2. Product-Service API를 호출하여 상품 정보 목록 조회
        List<ProductResponseDto> productInfos = getProductInfos(productIds);

        // 3. 상품 정보를 빠르게 조회할 수 있도록 Map으로 변환
        Map<Long, ProductResponseDto> productMap = productInfos.stream()
                .collect(Collectors.toMap(ProductResponseDto::productId, product -> product));

        // 4. OrderItem Entity 목록 생성 및 재고 확인
        List<OrderItem> orderItems = requestDto.items().stream()
                .map(itemDto -> {
                    ProductResponseDto product = productMap.get(itemDto.productId());
                    // 조회된 상품이 없거나, 재고가 부족한 경우 예외 처리 (실제로는 재고 확인 로직도 필요)
                    if (product == null) {
                        throw new IllegalArgumentException("상품 정보를 찾을 수 없습니다: " + itemDto.productId());
                    }
                    // OrderItem 생성 시, 클라이언트가 보낸 가격이 아닌 서버에서 조회한 가격을 사용
                    return new OrderItem(product.productId(), product.price(), itemDto.quantity());
                })
                .toList();

        // 5. Order Entity 생성
        String orderId = UUID.randomUUID().toString();
        Order order = Order.createOrder(orderId, requestDto.userId(), orderItems);

        // 6. DB에 주문 정보 저장
        Order savedOrder = orderRepository.save(order);

        // 7. Kafka에 발행할 OrderCreatedEvent 생성
        List<OrderItemPayload> itemPayloads = savedOrder.getOrderItems().stream()
                .map(item -> new OrderItemPayload(item.getProductId(), item.getQuantity(), item.getPrice()))
                .toList();

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getOrderId(),
                savedOrder.getUserId(),
                savedOrder.getTotalPrice(),
                savedOrder.getOrderedAt(),
                itemPayloads
        );

        // 8. Kafka로 이벤트 발행
        kafkaProducer.send("order-created", event);

        // 9. 최종 응답 DTO 생성 및 반환
        return new OrderResponseDto(
                savedOrder.getOrderId(),
                savedOrder.getStatus().name(),
                savedOrder.getTotalPrice(),
                savedOrder.getOrderedAt()
        );
    }

    //productId 여러개를 받아 상품을 리스트로 반환. ("api/v1/product/213,231,222")일 경우 한번에 조회
    private List<ProductResponseDto> getProductInfos(List<Long> productIds) {
        String idsString = productIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        ProductResponseDto[] response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/products")
                        .queryParam("ids", idsString)
                        .build())
                .retrieve()
                .bodyToMono(ProductResponseDto[].class)
                .block();

        return response == null ? List.of() : Arrays.asList(response); // true일 경우 빈리스트, false일 경우 리스트 반환
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponseDto getOrderByOrderId(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        return OrderDetailResponseDto.fromEntity(order);
    }

    @Override
    @Transactional
    public void cancelOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 주문은 취소할 수 없습니다.");
        }

        order.cancel();

        List<OrderItemPayload> itemPayloads = order.getOrderItems().stream()
                .map(item -> new OrderItemPayload(item.getProductId(), item.getQuantity(), item.getPrice()))
                .toList();

        OrderCancelledEvent event = new OrderCancelledEvent(
                order.getOrderId(),
                order.getUserId(),
                order.getTotalPrice(),
                order.getOrderedAt(), // 생성 시각 사용
                itemPayloads
        );

        kafkaProducer.send("order-cancelled", event);
    }
}
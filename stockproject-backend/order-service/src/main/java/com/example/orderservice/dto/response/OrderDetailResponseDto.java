// OrderDetailResponseDto.java (신규 파일)
package com.example.orderservice.dto.response;

import com.example.orderservice.domain.Order;
import com.example.orderservice.domain.OrderItem;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponseDto(
        String orderId,
        Long userId,
        String status,
        Long totalPrice,
        LocalDateTime orderedAt,
        List<OrderItemInfo> items
) {
    public static OrderDetailResponseDto fromEntity(Order order) {
        List<OrderItemInfo> itemInfos = order.getOrderItems().stream()
                .map(OrderItemInfo::fromEntity)
                .toList();

        return new OrderDetailResponseDto(
                order.getOrderId(),
                order.getUserId(),
                order.getStatus().name(),
                order.getTotalPrice(),
                order.getOrderedAt(),
                itemInfos
        );
    }

    // 내부 DTO
    public record OrderItemInfo(String productId, Integer quantity, Long price) {
        public static OrderItemInfo fromEntity(OrderItem item) {
            return new OrderItemInfo(item.getProductId(), item.getQuantity(), item.getPrice());
        }
    }
}
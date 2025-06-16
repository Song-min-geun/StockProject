package com.example.orderservice.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // 'order'는 SQL 예약어인 경우가 많아 'orders' 사용
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 외부에 노출할 비즈니스 ID (UUID 등)
    @Column(unique = true, nullable = false)
    private String orderId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long totalPrice;

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 저장
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    // Order가 OrderItems의 생명주기를 관리합니다. (Cascade)
    // 부모(Order)가 사라지면 자식(OrderItems)도 함께 사라집니다. (orphanRemoval)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    //== 생성 메소드 ==//
    public static Order createOrder(String orderId, Long userId, List<OrderItem> orderItems) {
        Order order = new Order();
        order.orderId = orderId;
        order.userId = userId;
        order.status = OrderStatus.PENDING;
        order.orderedAt = LocalDateTime.now();

        long totalPrice = 0;
        for (OrderItem item : orderItems) {
            order.addOrderItem(item);
            totalPrice += (item.getPrice() * item.getQuantity());
        }
        order.totalPrice = totalPrice;
        
        return order;
    }

    //== 연관관계 편의 메소드 ==//
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}
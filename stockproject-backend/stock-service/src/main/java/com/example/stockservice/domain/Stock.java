package com.example.stockservice.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

// redis key : "stock:101"
@RedisHash("stock")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    // ⭐️ productId를 Redis의 Key로 사용하기 위해 @Id로 지정합니다.
    @Id
    private String productId;

    private Long quantity;

    public Stock(String productId, Long quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    //== 비즈니스 로직 ==//
    public void decrease(Long requestedQuantity) {
        if (this.quantity < requestedQuantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.quantity -= requestedQuantity;
    }

    public void increase(Long quantity) {
        this.quantity += quantity;
    }
}
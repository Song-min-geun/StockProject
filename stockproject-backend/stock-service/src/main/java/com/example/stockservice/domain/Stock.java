package com.example.stockservice.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long productId;

    private Long quantity;

    //== 비즈니스 로직 ==//
    /**
     * 재고를 감소시킵니다.
     * @param requestedQuantity 감소시킬 수량
     */
    public void decrease(Long requestedQuantity) {
        if (this.quantity < requestedQuantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.quantity -= requestedQuantity;
    }
}
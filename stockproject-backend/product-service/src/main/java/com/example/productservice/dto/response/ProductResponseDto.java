package com.example.productservice.dto.response;

import com.example.productservice.domain.Product;

public record ProductResponseDto (
    String productId,
    String name,
    Long price
){
    public static ProductResponseDto fromEntity(Product product){
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getPrice()
        );
    }
}


package com.example.productservice.dto.request;

//제품 처음 등록할시 필요한 dto
public record ProductRegistrationRequest(
        String name,
        Long price,
        int initialStock
){}

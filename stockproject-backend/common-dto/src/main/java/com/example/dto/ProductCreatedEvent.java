package com.example.dto;

public record ProductCreatedEvent(
        String productId,
        int initialStock
){
};

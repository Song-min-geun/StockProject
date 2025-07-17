package com.example.productservice.service;

import com.example.productservice.domain.Product;
import com.example.productservice.dto.request.ProductRegistrationRequest;
import com.example.productservice.dto.response.ProductResponseDto;

import java.util.List;

public interface ProductService {
    Product registerProduct(ProductRegistrationRequest request);

    // 단건 조회를 위한 메서드 추가
    ProductResponseDto findProductById(String productId);

    // ID 타입을 Long에서 String으로 변경
    List<ProductResponseDto> findProductsByIds(List<String> ids);
}
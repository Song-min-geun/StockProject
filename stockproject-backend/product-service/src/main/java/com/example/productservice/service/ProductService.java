package com.example.productservice.service;

import com.example.productservice.domain.Product;
import com.example.productservice.dto.request.ProductRegistrationRequest;
import com.example.productservice.dto.response.ProductResponseDto;

import java.util.List;

public interface ProductService {
    Product registerProduct(ProductRegistrationRequest request);

    List<ProductResponseDto> findProductsByIds(Iterable<String> ids);
}
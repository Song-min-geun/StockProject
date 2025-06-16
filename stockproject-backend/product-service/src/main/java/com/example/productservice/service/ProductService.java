package com.example.productservice.service;

import com.example.productservice.domain.Product;
import com.example.productservice.dto.request.ProductRegistrationRequest;
import com.example.productservice.dto.response.ProductResponseDto;
import com.example.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductService {
    Product registerProduct(ProductRegistrationRequest request);

    List<ProductResponseDto> findProductsByIds(List<Long> ids);
}
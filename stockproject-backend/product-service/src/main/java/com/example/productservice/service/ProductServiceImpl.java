package com.example.productservice.service;

import com.example.productservice.domain.Product;
import com.example.productservice.dto.request.ProductRegistrationRequest;
import com.example.productservice.dto.response.ProductResponseDto;
import com.example.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Product registerProduct(ProductRegistrationRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .price(request.price())
                .stock(request.initialStock())
                .build();
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto findProductById(String productId) {
        // Repository의 내장 메서드인 findById 사용
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 상품을 찾을 수 없습니다: " + productId));
        return ProductResponseDto.fromEntity(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> findProductsByIds(List<String> ids) { // ID 타입을 String으로 수정
        // Repository의 내장 메서드인 findAllById 사용
        List<Product> products = productRepository.findAllById(ids);
        return products.stream()
                .map(ProductResponseDto::fromEntity)
                .toList();
    }
}
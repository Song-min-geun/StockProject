package com.example.productservice.service;

import com.example.productservice.domain.Product;
import com.example.productservice.dto.request.ProductRegistrationRequest; // 정확한 DTO 임포트
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
    public Product registerProduct(ProductRegistrationRequest request) { // DTO 타입 수정
        Product product = Product.builder()
                .name(request.name())
                .price(request.price())
                .stock(request.initialStock())
                .build();

        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> findProductsByIds(Iterable<String> ids) { // ID 타입을 String으로 수정
        List<Product> products = productRepository.findAllById(ids);
        return products.stream()
                .map(ProductResponseDto::fromEntity)
                .toList();
    }
}
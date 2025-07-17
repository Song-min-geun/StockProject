package com.example.productservice.service;

import com.example.productservice.domain.Product;
import com.example.productservice.dto.request.ProductRegistrationRequest;
import com.example.productservice.dto.response.ProductResponseDto;
import com.example.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Spring의 Transactional 사용

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
    @Transactional(readOnly = true) // 조회 전용 트랜잭션은 readOnly = true 옵션으로 성능 최적화
    public List<ProductResponseDto> findProductsByIds(List<String> ids) {
        // Repository를 호출해 Entity 목록을 가져옵니다.
        List<Product> products = productRepository.findAllByIdIn(ids);

        // Entity 목록을 DTO 목록으로 변환하여 반환합니다.
        return products.stream()
                .map(ProductResponseDto::fromEntity)
                .toList();
    }
}
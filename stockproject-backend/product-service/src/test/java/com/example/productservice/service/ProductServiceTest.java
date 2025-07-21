package com.example.productservice.service;

import com.example.productservice.domain.Product;
import com.example.productservice.dto.request.ProductRegistrationRequest;
import com.example.productservice.dto.response.ProductResponseDto;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles; // 이 부분을 import 합니다.

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test") // "test" 프로필을 활성화하여 테스트 전용 설정을 사용하도록 지정
@DataMongoTest
class ProductServiceTest {

    @Autowired
    private ProductRepository productRepository;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository);
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적인 요청이 오면 상품을 성공적으로 등록한다.")
    void registerProduct_Success() {
        // given
        ProductRegistrationRequest request = new ProductRegistrationRequest("테스트 상품", 10000L, 100);

        // when
        Product registeredProduct = productService.registerProduct(request);

        // then
        assertThat(registeredProduct).isNotNull();
        assertThat(registeredProduct.getId()).isNotNull();
        assertThat(registeredProduct.getName()).isEqualTo("테스트 상품");
        assertThat(registeredProduct.getPrice()).isEqualTo(10000L);
        assertThat(registeredProduct.getStock()).isEqualTo(100);
    }

    @Test
    @DisplayName("상품을 등록하면, 데이터베이스에서 해당 상품을 조회할 수 있다.")
    void registerProduct_And_FindItInDB() {
        // given
        ProductRegistrationRequest request = new ProductRegistrationRequest("DB 저장 테스트 상품", 5000L, 50);

        // when
        Product registeredProduct = productService.registerProduct(request);

        // then
        assertThat(registeredProduct).isNotNull();
        Optional<Product> foundProductOpt = productRepository.findById(registeredProduct.getId());

        assertTrue(foundProductOpt.isPresent(), "등록된 상품을 DB에서 찾을 수 있어야 합니다.");
        Product foundProduct = foundProductOpt.get();

        assertThat(foundProduct.getId()).isEqualTo(registeredProduct.getId());
        assertThat(foundProduct.getName()).isEqualTo("DB 저장 테스트 상품");
        assertThat(foundProduct.getPrice()).isEqualTo(5000L);
        assertThat(foundProduct.getStock()).isEqualTo(50);
    }

    @Test
    @DisplayName("상품 ID로 단일 상품을 성공적으로 조회한다.")
    void findProductById_Success() {
        // given
        Product savedProduct = productRepository.save(Product.builder()
                .name("조회용 상품")
                .price(12000L)
                .stock(10)
                .build());
        String productId = savedProduct.getId();

        // when
        ProductResponseDto foundProductDto = productService.findProductById(productId);

        // then
        assertThat(foundProductDto).isNotNull();
        assertThat(foundProductDto.productId()).isEqualTo(productId);
        assertThat(foundProductDto.name()).isEqualTo("조회용 상품");
        assertThat(foundProductDto.price()).isEqualTo(12000L);
    }
}
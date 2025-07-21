package com.example.productservice.service;

import com.example.productservice.domain.Product;
import com.example.productservice.dto.request.ProductRegistrationRequest;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    // 각 테스트가 끝난 후 DB를 깨끗하게 정리합니다.
    @AfterEach
    void tearDown() {
        // 1. deleteAllInBatch()는 JPA 전용이므로, MongoRepository의 deleteAll()을 사용합니다.
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적인 요청이 오면 상품을 성공적으로 등록한다.")
    void registerProduct_Success() {
        // Given (주어진 상황)
        final String productName = "테스트 상품";
        final long price = 10000L;
        final int initialStock = 100;
        final ProductRegistrationRequest request = new ProductRegistrationRequest(productName, price, initialStock);

        // When (행동)
        final Product registeredProduct = productService.registerProduct(request);

        // Then (결과)
        assertThat(registeredProduct).isNotNull();
        assertThat(registeredProduct.getId()).isNotNull(); // ID가 생성되었는지 확인
        assertThat(registeredProduct.getName()).isEqualTo(productName);
        assertThat(registeredProduct.getPrice()).isEqualTo(price);
        assertThat(registeredProduct.getStock()).isEqualTo(initialStock);
    }

    @Test
    @DisplayName("상품을 등록하면, 데이터베이스에서 해당 상품을 조회할 수 있다.")
    void registerProduct_And_Find_From_Database_Success() {
        // Given
        final String productName = "실제 저장될 상품";
        final long price = 25000L;
        final int initialStock = 50;
        final ProductRegistrationRequest request = new ProductRegistrationRequest(productName, price, initialStock);

        // When
        Product savedProduct = productService.registerProduct(request);

        // Then
        assertThat(savedProduct).isNotNull();

        // 2. 저장된 객체의 실제 String ID를 가져옵니다.
        String savedProductId = savedProduct.getId();

        // 3. 그 ID를 사용해 데이터베이스에서 직접 조회합니다.
        Product foundProduct = productRepository.findById(savedProductId)
                .orElseThrow(() -> new AssertionError("저장된 상품을 찾을 수 없습니다."));

        assertThat(foundProduct.getName()).isEqualTo(productName);
        assertThat(foundProduct.getPrice()).isEqualTo(price);
        assertThat(foundProduct.getStock()).isEqualTo(initialStock);
    }
}
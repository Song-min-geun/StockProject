package com.example.productservice.service;

import com.example.productservice.domain.Product;
import com.example.productservice.dto.request.ProductRegistrationRequest;
import com.example.productservice.dto.response.ProductResponseDto;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test; // JUnit 5 어노테이션으로 통일
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class ProductServiceTest {

    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.4.2"));

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    // 각 테스트 후 DB를 초기화하여 테스트 간 독립성을 보장합니다.
    @AfterEach
    void cleanUp() {
        this.productRepository.deleteAll();
    }

    @Test
    @DisplayName("상품을 성공적으로 등록한다")
    void registerProduct_Success() {
        // given
        ProductRegistrationRequest requestDto = new ProductRegistrationRequest(
                "TestProduct01",
                10000L,
                100
        );

        // when
        productService.registerProduct(requestDto);

        // then
        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(1);

        Product savedProduct = products.get(0);
        assertThat(savedProduct.getName()).isEqualTo("TestProduct01");
        assertThat(savedProduct.getPrice()).isEqualTo(10000L);
        assertThat(savedProduct.getStock()).isEqualTo(100);
    }

    @Test
    @DisplayName("상품 ID로 특정 상품을 성공적으로 조회한다")
    void findProductById_Success() {
        // given
        Product savedProduct = productRepository.save(Product.builder().name("Test Product").price(1L).stock(100).build());
        String savedId = savedProduct.getId();

        // when
        ProductResponseDto foundProduct = productService.findProductById(savedId);

        // then
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.productId()).isEqualTo(savedId);
        assertThat(foundProduct.name()).isEqualTo("Test Product");
    }

    @Test
    @DisplayName("존재하지 않는 상품 ID로 조회 시 예외가 발생한다")
    void findProductById_NotFound_ThrowsException() {
        // given
        String nonExistentId = "non-existent-id";

        // when & then
        // ProductServiceImpl에서 던지는 예외와 메시지가 일치하는지 검증합니다.
        assertThatThrownBy(() -> productService.findProductById(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 ID의 상품을 찾을 수 없습니다: " + nonExistentId);
    }
}
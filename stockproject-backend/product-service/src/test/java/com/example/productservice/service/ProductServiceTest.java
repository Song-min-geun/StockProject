package com.example.productservice.service;

import com.example.productservice.domain.Product;
import com.example.productservice.dto.request.ProductRegistrationRequest;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@DisplayName("정상적인 상품등록이면 등록한다.")
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void tearDown(){
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적인 요청이 오면 상품을 성공적으로 등록한다.")
    void registerProduct_Success() {
        // Given (주어진 상황)
        // 클라이언트가 상품 등록을 위해 아래와 같은 정보를 전달했다고 가정합니다.
        final String productName = "테스트 상품";
        final long price = 10000L;
        final int initialStock = 100;

        // ProductRegistrationRequest DTO (데이터 전송 객체)를 만듭니다.
        final ProductRegistrationRequest request = new ProductRegistrationRequest(productName, price, initialStock);

        // When (행동)
        // 아직 존재하지 않는 ProductService의 registerProduct 메소드를 호출합니다.
        final Product registeredProduct = productService.registerProduct(request);

        // Then (결과)
        // 반환된 registeredProduct 객체가 우리가 기대하는 상태를 만족하는지 검증합니다.
        assertThat(registeredProduct).isNotNull();
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
        assertThat(savedProduct).isNotNull(); // 1차 검증: 반환된 객체가 null이 아닌지

        // 2차 검증: 데이터베이스에서 직접 조회
        Product foundProduct = productRepository.findById(savedProduct.getId()).orElseThrow();

        assertThat(foundProduct.getName()).isEqualTo(productName);
        assertThat(foundProduct.getPrice()).isEqualTo(price);
        assertThat(foundProduct.getStock()).isEqualTo(initialStock);
    }
}

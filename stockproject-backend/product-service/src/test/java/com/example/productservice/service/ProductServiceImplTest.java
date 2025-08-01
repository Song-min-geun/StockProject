package com.example.productservice.service;

import com.example.dto.ProductCreatedEvent;
import com.example.productservice.domain.Product;
import com.example.productservice.dto.request.ProductRegistrationRequestDto;
import com.example.productservice.dto.response.ProductResponseDto;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @DisplayName("상품 등록: 성공 - 상품을 성공적으로 등록하고 Kafka 이벤트를 발행한다.")
    void registerProduct_Success() {
        // given
        String productName = "테스트 상품";
        long price = 10000L;
        int initialStock = 100;
        ProductRegistrationRequestDto requestDto = new ProductRegistrationRequestDto(productName, price, initialStock);
        
        Product savedProduct = Product.builder()
                .id("product-id-101")
                .name(productName)
                .price(price)
                .stock(initialStock)
                .build();
        
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        
        // when
        Product result = productService.registerProduct(requestDto);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("product-id-101");
        assertThat(result.getName()).isEqualTo(productName);
        assertThat(result.getPrice()).isEqualTo(price);
        assertThat(result.getStock()).isEqualTo(initialStock);
        
        // Verify Kafka event was sent
        ArgumentCaptor<ProductCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ProductCreatedEvent.class);
        verify(kafkaTemplate).send(eq("product-created"), eventCaptor.capture());
        
        ProductCreatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.productId()).isEqualTo("product-id-101");
        assertThat(capturedEvent.initialStock()).isEqualTo(initialStock);
    }

    @Test
    @DisplayName("상품 삭제: 성공 - 상품을 성공적으로 삭제하고 Kafka 이벤트를 발행한다.")
    void deleteProduct_Success() {
        // given
        String productId = "product-id-101";
        doNothing().when(productRepository).deleteById(productId);
        
        // when
        productService.deleteProduct(productId);
        
        // then
        verify(productRepository).deleteById(productId);
        verify(kafkaTemplate).send(eq("product-deleted"), eq(productId));
    }

    @Test
    @DisplayName("상품 조회: 성공 - ID로 상품을 성공적으로 조회한다.")
    void findProductById_Success() {
        // given
        String productId = "product-id-101";
        Product product = Product.builder()
                .id(productId)
                .name("테스트 상품")
                .price(10000L)
                .stock(100)
                .build();
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        
        // when
        ProductResponseDto result = productService.findProductById(productId);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.name()).isEqualTo("테스트 상품");
        assertThat(result.price()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("상품 조회: 실패 - 존재하지 않는 ID로 조회 시 예외가 발생한다.")
    void findProductById_Fail_ProductNotFound() {
        // given
        String nonExistingProductId = "non-existing-product-id";
        when(productRepository.findById(nonExistingProductId)).thenReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> productService.findProductById(nonExistingProductId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 ID의 상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("상품 목록 조회: 성공 - ID 목록으로 상품들을 성공적으로 조회한다.")
    void findProductsByIds_Success() {
        // given
        List<String> productIds = Arrays.asList("product-id-101", "product-id-102");
        
        Product product1 = Product.builder()
                .id("product-id-101")
                .name("테스트 상품 1")
                .price(10000L)
                .stock(100)
                .build();
        
        Product product2 = Product.builder()
                .id("product-id-102")
                .name("테스트 상품 2")
                .price(20000L)
                .stock(200)
                .build();
        
        when(productRepository.findAllById(productIds)).thenReturn(Arrays.asList(product1, product2));
        
        // when
        List<ProductResponseDto> results = productService.findProductsByIds(productIds);
        
        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).productId()).isEqualTo("product-id-101");
        assertThat(results.get(0).name()).isEqualTo("테스트 상품 1");
        assertThat(results.get(0).price()).isEqualTo(10000L);
        
        assertThat(results.get(1).productId()).isEqualTo("product-id-102");
        assertThat(results.get(1).name()).isEqualTo("테스트 상품 2");
        assertThat(results.get(1).price()).isEqualTo(20000L);
    }

    @Test
    @DisplayName("상품 목록 조회: 성공 - 일부 상품만 존재하는 경우 존재하는 상품만 반환한다.")
    void findProductsByIds_PartialSuccess() {
        // given
        List<String> productIds = Arrays.asList("product-id-101", "non-existing-product-id");
        
        Product product1 = Product.builder()
                .id("product-id-101")
                .name("테스트 상품 1")
                .price(10000L)
                .stock(100)
                .build();
        
        when(productRepository.findAllById(productIds)).thenReturn(Arrays.asList(product1));
        
        // when
        List<ProductResponseDto> results = productService.findProductsByIds(productIds);
        
        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).productId()).isEqualTo("product-id-101");
        assertThat(results.get(0).name()).isEqualTo("테스트 상품 1");
        assertThat(results.get(0).price()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("상품 목록 조회: 성공 - 빈 ID 목록으로 조회 시 빈 목록을 반환한다.")
    void findProductsByIds_EmptyList() {
        // given
        List<String> emptyProductIds = Arrays.asList();
        when(productRepository.findAllById(emptyProductIds)).thenReturn(Arrays.asList());
        
        // when
        List<ProductResponseDto> results = productService.findProductsByIds(emptyProductIds);
        
        // then
        assertThat(results).isEmpty();
    }
}
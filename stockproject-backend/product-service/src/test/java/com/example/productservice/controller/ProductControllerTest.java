package com.example.productservice.controller;

import com.example.productservice.domain.Product;
import com.example.productservice.dto.request.ProductRegistrationRequestDto;
import com.example.productservice.dto.response.ProductResponseDto;
import com.example.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    @DisplayName("POST /api/v1/product 요청 시, 상품을 등록하고 201 Created를 반환한다.")
    @WithMockUser
    void registerProduct_Success() throws Exception {
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
        
        ProductResponseDto responseDto = ProductResponseDto.fromEntity(savedProduct);
        
        when(productService.registerProduct(any(ProductRegistrationRequestDto.class))).thenReturn(savedProduct);
        
        // when & then
        mockMvc.perform(post("/api/v1/product")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/product/product-id-101"))
                .andExpect(jsonPath("$.productId").value("product-id-101"))
                .andExpect(jsonPath("$.name").value(productName))
                .andExpect(jsonPath("$.price").value(price));
    }

    @Test
    @DisplayName("GET /api/v1/product/{productId} 요청 시, 상품 정보를 조회하고 200 OK를 반환한다.")
    @WithMockUser
    void findProductById_Success() throws Exception {
        // given
        String productId = "product-id-101";
        ProductResponseDto responseDto = new ProductResponseDto(productId, "테스트 상품", 10000L);
        
        when(productService.findProductById(productId)).thenReturn(responseDto);
        
        // when & then
        mockMvc.perform(get("/api/v1/product/{productId}", productId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.name").value("테스트 상품"))
                .andExpect(jsonPath("$.price").value(10000L));
    }

    @Test
    @DisplayName("GET /api/v1/product/{productId} 요청 시, 상품이 존재하지 않으면 400 Bad Request를 반환한다.")
    @WithMockUser
    void findProductById_Fail_ProductNotFound() throws Exception {
        // given
        String nonExistingProductId = "non-existing-product-id";
        when(productService.findProductById(nonExistingProductId))
                .thenThrow(new IllegalArgumentException("해당 ID의 상품을 찾을 수 없습니다: " + nonExistingProductId));
        
        // when & then
        mockMvc.perform(get("/api/v1/product/{productId}", nonExistingProductId)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/product/list 요청 시, 상품 목록을 조회하고 200 OK를 반환한다.")
    @WithMockUser
    void findProductsByIds_Success() throws Exception {
        // given
        List<String> productIds = Arrays.asList("product-id-101", "product-id-102");
        
        ProductResponseDto product1 = new ProductResponseDto("product-id-101", "테스트 상품 1", 10000L);
        ProductResponseDto product2 = new ProductResponseDto("product-id-102", "테스트 상품 2", 20000L);
        
        when(productService.findProductsByIds(productIds)).thenReturn(Arrays.asList(product1, product2));
        
        // when & then
        mockMvc.perform(get("/api/v1/product/list")
                        .with(csrf())
                        .param("ids", "product-id-101", "product-id-102"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value("product-id-101"))
                .andExpect(jsonPath("$[0].name").value("테스트 상품 1"))
                .andExpect(jsonPath("$[0].price").value(10000L))
                .andExpect(jsonPath("$[1].productId").value("product-id-102"))
                .andExpect(jsonPath("$[1].name").value("테스트 상품 2"))
                .andExpect(jsonPath("$[1].price").value(20000L));
    }

    @Test
    @DisplayName("DELETE /api/v1/product/{productId} 요청 시, 상품을 삭제하고 204 No Content를 반환한다.")
    @WithMockUser
    void deleteProduct_Success() throws Exception {
        // given
        String productId = "product-id-101";
        doNothing().when(productService).deleteProduct(productId);
        
        // when & then
        mockMvc.perform(delete("/api/v1/product/{productId}", productId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(productService).deleteProduct(productId);
    }
}
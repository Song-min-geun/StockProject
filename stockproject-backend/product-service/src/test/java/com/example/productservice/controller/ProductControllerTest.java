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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles; // 이 부분을 import 합니다.
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test") // "test" 프로필을 활성화하여 테스트 전용 설정을 사용하도록 지정
@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    @DisplayName("상품 등록 API: 성공 시 201 Created와 함께 상품 정보 및 Location 헤더 반환")
    @WithMockUser
    void registerProduct_Success() throws Exception {
        // given
        ProductRegistrationRequestDto requestDto = new ProductRegistrationRequestDto("테스트 상품", 10000L, 100);
        String fakeMongoId = "60c72b2f9b1e8b3b4c8b4567";

        Product savedProduct = Product.builder()
                .id(fakeMongoId)
                .name(requestDto.name())
                .price(requestDto.price())
                .stock(requestDto.initialStock())
                .build();

        when(productService.registerProduct(any(ProductRegistrationRequestDto.class))).thenReturn(savedProduct);

        // when & then
        mockMvc.perform(post("/api/v1/product")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/product/" + fakeMongoId))
                .andExpect(jsonPath("$.productId").value(fakeMongoId))
                .andExpect(jsonPath("$.name").value("테스트 상품"));
    }

    @Test
    @DisplayName("상품 단건 조회 API: 성공 시 200 OK와 함께 상품 정보 반환")
    @WithMockUser
    void findProductById_Success() throws Exception {
        // given
        String productId = "some-product-id";
        ProductResponseDto responseDto = ProductResponseDto.fromEntity(
                Product.builder().id(productId).name("조회된 상품").price(5000L).stock(10).build()
        );

        when(productService.findProductById(productId)).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/product/{productId}", productId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.name").value("조회된 상품"));
    }


    @Test
    @DisplayName("상품 ID 목록 조회 API: 성공 시 200 OK와 함께 상품 목록 반환")
    @WithMockUser
    void findProductsByIds_Success() throws Exception {
        // given
        List<String> requestIds = List.of("id-1", "id-2");
        List<ProductResponseDto> responseDtos = List.of(
                ProductResponseDto.fromEntity(Product.builder().id("id-1").name("상품1").price(1000L).build()),
                ProductResponseDto.fromEntity(Product.builder().id("id-2").name("상품2").price(2000L).build())
        );

        when(productService.findProductsByIds(anyList())).thenReturn(responseDtos);

        // when & then
        mockMvc.perform(get("/api/v1/product/list")
                        .with(csrf())
                        .param("ids", String.join(",", requestIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productId").value("id-1"))
                .andExpect(jsonPath("$[1].name").value("상품2"));
    }
}
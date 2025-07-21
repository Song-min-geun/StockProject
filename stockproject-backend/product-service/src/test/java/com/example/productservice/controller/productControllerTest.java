package com.example.productservice.controller;

import com.example.productservice.domain.Product;
import com.example.productservice.dto.request.ProductRegistrationRequest;
import com.example.productservice.dto.response.ProductResponseDto;
import com.example.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class productControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean// ProductController가 의존하는 ProductService를 가짜 객체로 주입
    private ProductService productService;

    @Test
    @DisplayName("상품 등록 API: 성공 시 201 Created와 함께 상품 정보 및 Location 헤더 반환")
    @WithMockUser // Spring Security 인증을 통과하기 위함
    void registerProduct_Success() throws Exception {
        // given
        ProductRegistrationRequest requestDto = new ProductRegistrationRequest("테스트 상품", 10000L, 100);

        // 1. 테스트용 가짜 ID를 미리 정의합니다.
        String fakeMongoId = "60c72b2f9b1e8b3b4c8b4567";

        Product savedProduct = Product.builder()
                .id(fakeMongoId) // 2. Long이 아닌 String 타입의 ID를 사용합니다.
                .name(requestDto.name())
                .price(requestDto.price())
                .stock(requestDto.initialStock())
                .build();

        // productService.registerProduct가 어떤 요청으로든 호출되면, 위에서 만든 savedProduct 객체를 반환하도록 설정
        when(productService.registerProduct(any(ProductRegistrationRequest.class))).thenReturn(savedProduct);

        // when & then
        mockMvc.perform(post("/api/v1/product")
                        .with(csrf()) // Spring Security의 CSRF 보호 우회
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) // HTTP 201 Created 상태 확인
                .andExpect(header().string("Location", "http://localhost/api/v1/product/" + fakeMongoId)) // Location 헤더 검증
                .andExpect(jsonPath("$.productId").value(fakeMongoId)) // 3. String ID로 검증
                .andExpect(jsonPath("$.name").value("테스트 상품"));
    }

    @Test
    @DisplayName("상품 ID 목록 조회 API: 성공 시 200 OK와 함께 상품 목록 반환")
    @WithMockUser
    void findProductsByIds_Success() throws Exception {
        // given
        // 1. 모든 ID를 String 타입으로 변경합니다.
        List<String> ids = List.of("id-1", "id-2");
        List<ProductResponseDto> responseDtos = List.of(
                ProductResponseDto.fromEntity(Product.builder().id("id-1").name("상품1").price(1000L).build()),
                ProductResponseDto.fromEntity(Product.builder().id("id-2").name("상품2").price(2000L).build())
        );

        when(productService.findProductsByIds(anyList())).thenReturn(responseDtos);

        // when & then
        // 2. 컨트롤러가 POST로 변경되었으므로, Body에 ID 목록을 담아 보냅니다.
        mockMvc.perform(post("/api/v1/product/list")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk()) // HTTP 200 OK 상태 확인
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productId").value("id-1")) // 3. String ID로 검증
                .andExpect(jsonPath("$[1].name").value("상품2"));
    }
}
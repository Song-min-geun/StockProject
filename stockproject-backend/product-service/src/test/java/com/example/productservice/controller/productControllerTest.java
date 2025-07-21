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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(ProductController.class) // ProductController를 테스트 대상으로 지정
@DisplayName("ProductController API 테스트")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean // 가짜 ProductService를 Spring 컨테이너에 등록
    private ProductService productService;

    @Test
    @DisplayName("상품 등록 API: 성공 시 201 Created와 함께 상품 정보 및 Location 헤더 반환")
    @WithMockUser
    void registerProduct_Success() throws Exception {
        // given
        ProductRegistrationRequest requestDto = new ProductRegistrationRequest("테스트 상품", 10000L, 100);
        Product savedProduct = Product.builder()
                .id(1L)
                .name(requestDto.name())
                .price(requestDto.price())
                .stock(requestDto.initialStock())
                .build();

        // productService.registerProduct가 호출되면 위에서 만든 savedProduct 객체를 반환하도록 설정
        when(productService.registerProduct(any(ProductRegistrationRequest.class))).thenReturn(savedProduct);

        // when & then
        mockMvc.perform(post("/api/v1/product")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) // HTTP 201 Created 상태 확인
                .andExpect(header().exists("Location")) // Location 헤더 존재 여부 확인
                .andExpect(jsonPath("$.productId").value(1L)) // 응답 JSON의 필드 값 확인
                .andExpect(jsonPath("$.name").value("테스트 상품"));
    }

    @Test
    @DisplayName("상품 ID 목록 조회 API: 성공 시 200 OK와 함께 상품 목록 반환")
    @WithMockUser
    void findProductsByIds_Success() throws Exception {
        // given
        List<Long> ids = List.of(1L, 2L);
        List<ProductResponseDto> responseDtos = List.of(
                new ProductResponseDto(1L, "상품1", 1000L),
                new ProductResponseDto(2L, "상품2", 2000L)
        );

        // productService.findProductsByIds가 호출되면 위에서 만든 responseDtos를 반환하도록 설정
        when(productService.findProductsByIds(anyList())).thenReturn(responseDtos);

        // when & then
        mockMvc.perform(get("/api/v1/product")
                        .with(csrf())
                        .param("ids", "1,2")) // 요청 파라미터 설정
                .andExpect(status().isOk()) // HTTP 200 OK 상태 확인
                .andExpect(jsonPath("$").isArray()) // 응답이 배열인지 확인
                .andExpect(jsonPath("$.length()").value(2)) // 배열의 크기가 2인지 확인
                .andExpect(jsonPath("$[0].productId").value(1L))
                .andExpect(jsonPath("$[1].name").value("상품2"));
    }
}
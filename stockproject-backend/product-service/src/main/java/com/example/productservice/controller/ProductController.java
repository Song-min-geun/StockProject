package com.example.productservice.controller;

import com.example.productservice.domain.Product;
import com.example.productservice.dto.request.ProductRegistrationRequest;
import com.example.productservice.dto.response.ProductResponseDto;
import com.example.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/v1/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDto> registerProduct(@RequestBody ProductRegistrationRequest request) {
        // 1. 서비스를 호출하여 상품 등록
        Product registeredProduct = productService.registerProduct(request);

        // 2. 응답 DTO로 변환
        ProductResponseDto responseDto = ProductResponseDto.fromEntity(registeredProduct);

        // 3. 생성된 리소스의 URI를 생성하여 Location 헤더에 담아 201 Created 응답 반환
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registeredProduct.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDto);
    }


    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> findProductsByIds(
            @RequestParam("ids") List<Long> ids) {

        List<ProductResponseDto> products = productService.findProductsByIds(ids);
        return ResponseEntity.ok(products);
    }
}

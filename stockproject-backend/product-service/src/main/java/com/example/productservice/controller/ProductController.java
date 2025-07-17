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

    // 상품 등록
    @PostMapping
    public ResponseEntity<ProductResponseDto> registerProduct(@RequestBody ProductRegistrationRequest request) {
        Product registeredProduct = productService.registerProduct(request);
        ProductResponseDto responseDto = ProductResponseDto.fromEntity(registeredProduct);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registeredProduct.getId())
                .toUri();
        return ResponseEntity.created(location).body(responseDto);
    }

    // 상품 단건 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> findProductById(@PathVariable String productId) {
        ProductResponseDto product = productService.findProductById(productId);
        return ResponseEntity.ok(product);
    }
    // ===============================

    // 상품 여러 개 조회
    @PostMapping("/list")
    public ResponseEntity<List<ProductResponseDto>> findProductsByIds(@RequestBody List<String> ids) {
        List<ProductResponseDto> products = productService.findProductsByIds(ids);
        return ResponseEntity.ok(products);
    }
}
package com.example.productservice.controller;

import com.example.productservice.domain.Product;
import com.example.productservice.dto.request.ProductRegistrationRequest; // 정확한 DTO 임포트
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
    public ResponseEntity<ProductResponseDto> registerProduct(@RequestBody ProductRegistrationRequest request) { // DTO 타입 수정
        Product registeredProduct = productService.registerProduct(request);
        ProductResponseDto responseDto = ProductResponseDto.fromEntity(registeredProduct);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registeredProduct.getId())
                .toUri();
        return ResponseEntity.created(location).body(responseDto);
    }

    @PostMapping("/list") // GET에서 POST로 변경하여 Body로 ID 목록을 받음
    public ResponseEntity<List<ProductResponseDto>> findProductsByIds(@RequestBody Iterable<String> ids) { // ID 타입을 String으로 수정
        List<ProductResponseDto> products = productService.findProductsByIds(ids);
        return ResponseEntity.ok(products);
    }
}
package com.example.userservice.dto;

public record SignUpRequestDto(
        String email,
        String password
) {
}

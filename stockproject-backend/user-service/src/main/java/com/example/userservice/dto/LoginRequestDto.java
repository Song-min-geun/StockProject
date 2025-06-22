package com.example.userservice.dto;

public record LoginRequestDto(
        String email,
        String password
) {
}

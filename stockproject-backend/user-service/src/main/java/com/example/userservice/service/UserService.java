package com.example.userservice.service;

import com.example.userservice.dto.LoginRequestDto;
import com.example.userservice.dto.SignUpRequestDto;
import com.example.userservice.dto.TokenResponseDto;

public interface UserService {
    void signUp(SignUpRequestDto requestDto);

    TokenResponseDto login(LoginRequestDto requestDto);
}

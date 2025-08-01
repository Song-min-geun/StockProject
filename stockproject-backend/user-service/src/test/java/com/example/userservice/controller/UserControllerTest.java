package com.example.userservice.controller;

import com.example.userservice.dto.LoginRequestDto;
import com.example.userservice.dto.SignUpRequestDto;
import com.example.userservice.dto.TokenResponseDto;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/v1/users/signup 요청 시, 회원가입을 처리하고 201 Created를 반환한다.")
    void signUp_Success() throws Exception {
        // given
        String email = "test@example.com";
        String password = "password123";
        SignUpRequestDto requestDto = new SignUpRequestDto(email, password);
        
        doNothing().when(userService).signUp(any(SignUpRequestDto.class));
        
        // when & then
        mockMvc.perform(post("/api/v1/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());
        
        verify(userService).signUp(any(SignUpRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/signup 요청 시, 이미 사용 중인 이메일이면 400 Bad Request를 반환한다.")
    void signUp_Fail_EmailAlreadyExists() throws Exception {
        // given
        String email = "existing@example.com";
        String password = "password123";
        SignUpRequestDto requestDto = new SignUpRequestDto(email, password);
        
        doThrow(new IllegalArgumentException("이미 사용중인 이메일입니다."))
                .when(userService).signUp(any(SignUpRequestDto.class));
        
        // when & then
        mockMvc.perform(post("/api/v1/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
        
        verify(userService).signUp(any(SignUpRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login 요청 시, 로그인을 처리하고 200 OK와 토큰을 반환한다.")
    void login_Success() throws Exception {
        // given
        String email = "test@example.com";
        String password = "password123";
        String token = "jwt.token.string";
        
        LoginRequestDto requestDto = new LoginRequestDto(email, password);
        TokenResponseDto responseDto = new TokenResponseDto(token);
        
        when(userService.login(any(LoginRequestDto.class))).thenReturn(responseDto);
        
        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(token));
        
        verify(userService).login(any(LoginRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login 요청 시, 존재하지 않는 사용자면 400 Bad Request를 반환한다.")
    void login_Fail_UserNotFound() throws Exception {
        // given
        String email = "nonexistent@example.com";
        String password = "password123";
        
        LoginRequestDto requestDto = new LoginRequestDto(email, password);
        
        doThrow(new IllegalArgumentException("사용자를 찾을 수 없습니다."))
                .when(userService).login(any(LoginRequestDto.class));
        
        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
        
        verify(userService).login(any(LoginRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/login 요청 시, 비밀번호가 일치하지 않으면 400 Bad Request를 반환한다.")
    void login_Fail_IncorrectPassword() throws Exception {
        // given
        String email = "test@example.com";
        String password = "wrongPassword";
        
        LoginRequestDto requestDto = new LoginRequestDto(email, password);
        
        doThrow(new IllegalArgumentException("비밀번호가 일치하지 않습니다."))
                .when(userService).login(any(LoginRequestDto.class));
        
        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
        
        verify(userService).login(any(LoginRequestDto.class));
    }
}
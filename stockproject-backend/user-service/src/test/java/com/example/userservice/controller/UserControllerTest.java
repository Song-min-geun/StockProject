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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class) // UserController와 관련된 빈만 로드
@DisplayName("UserController API 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc; // API 요청을 시뮬레이션하기 위한 객체

    @Autowired
    private ObjectMapper objectMapper; // 객체를 JSON 문자열로 변환하기 위한 객체

    @MockitoBean // 가짜 UserService를 Spring 컨테이너에 등록
    private UserService userService;

    @Test
    @DisplayName("회원가입 API: 성공 시 201 Created 반환")
    @WithMockUser // Spring Security 인증을 통과한 것처럼 설정
    void signUp_Success() throws Exception {
        // given
        SignUpRequestDto requestDto = new SignUpRequestDto("test@example.com", "password123");
        // userService.signUp이 호출되어도 아무 일도 일어나지 않도록 설정 (void 메소드)
        doNothing().when(userService).signUp(any(SignUpRequestDto.class));

        // when & then
        mockMvc.perform(post("/api/v1/users/signup")
                        .with(csrf()) // CSRF 보호 기능 통과
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()); // HTTP 상태 코드가 201 Created인지 확인
    }

    @Test
    @DisplayName("로그인 API: 성공 시 200 OK와 함께 토큰 반환")
    @WithMockUser
    void login_Success() throws Exception {
        // given
        LoginRequestDto requestDto = new LoginRequestDto("test@example.com", "password123");
        TokenResponseDto tokenResponse = new TokenResponseDto("dummy-jwt-token");
        // userService.login이 호출되면 위에서 만든 tokenResponse 객체를 반환하도록 설정
        when(userService.login(any(LoginRequestDto.class))).thenReturn(tokenResponse);

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk()) // HTTP 상태 코드가 200 OK인지 확인
                .andExpect(jsonPath("$.accessToken").value("dummy-jwt-token")); // 응답 JSON의 accessToken 필드 값 확인
    }
}
package com.example.userservice.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);
        SecurityContextHolder.clearContext(); // 테스트 간 독립성을 위해 SecurityContext 초기화
    }

    @Test
    @DisplayName("유효한 토큰이 있는 경우, 인증 정보를 SecurityContext에 설정한다.")
    void doFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        // given
        String token = "valid.jwt.token";
        String bearerToken = "Bearer " + token;
        
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getAuthentication(token)).thenReturn(authentication);
        
        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // then
        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider).getAuthentication(token);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Authorization 헤더가 없는 경우, 인증 정보를 설정하지 않는다.")
    void doFilterInternal_NoAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn(null);
        
        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // then
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(jwtTokenProvider, never()).getAuthentication(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Bearer 형식이 아닌 토큰의 경우, 인증 정보를 설정하지 않는다.")
    void doFilterInternal_NonBearerToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // given
        String nonBearerToken = "NonBearer token";
        
        when(request.getHeader("Authorization")).thenReturn(nonBearerToken);
        
        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // then
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(jwtTokenProvider, never()).getAuthentication(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("유효하지 않은 토큰의 경우, 인증 정보를 설정하지 않는다.")
    void doFilterInternal_InvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // given
        String token = "invalid.jwt.token";
        String bearerToken = "Bearer " + token;
        
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);
        
        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // then
        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider, never()).getAuthentication(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("빈 Authorization 헤더의 경우, 인증 정보를 설정하지 않는다.")
    void doFilterInternal_EmptyAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn("");
        
        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // then
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(jwtTokenProvider, never()).getAuthentication(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
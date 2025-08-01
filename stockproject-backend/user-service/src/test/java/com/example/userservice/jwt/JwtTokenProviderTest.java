package com.example.userservice.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserDetails userDetails;

    private final String secretKey = Base64.getEncoder().encodeToString("testSecretKeyForJwtTokenProviderTest".getBytes());
    private final long expirationTime = 3600000; // 1 hour
    private final String email = "test@example.com";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secretKey, expirationTime, userDetailsService);
    }

    @Test
    @DisplayName("토큰 생성: 이메일로 JWT 토큰을 성공적으로 생성한다.")
    void generateToken_Success() {
        // when
        String token = jwtTokenProvider.generateToken(email);

        // then
        assertThat(token).isNotNull();
        assertThat(token.split("\\.").length).isEqualTo(3); // JWT 형식 (header.payload.signature)
    }

    @Test
    @DisplayName("토큰 검증: 유효한 토큰은 true를 반환한다.")
    void validateToken_ValidToken_ReturnsTrue() {
        // given
        String token = jwtTokenProvider.generateToken(email);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("토큰 검증: 만료된 토큰은 false를 반환한다.")
    void validateToken_ExpiredToken_ReturnsFalse() {
        // given
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider(secretKey, 0, userDetailsService);
        String expiredToken = shortExpirationProvider.generateToken(email);

        // when
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰 검증: 잘못된 형식의 토큰은 false를 반환한다.")
    void validateToken_InvalidToken_ReturnsFalse() {
        // given
        String invalidToken = "invalid.token.format";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("이메일 추출: 토큰에서 이메일을 성공적으로 추출한다.")
    void getEmailFromToken_Success() {
        // given
        String token = jwtTokenProvider.generateToken(email);

        // when
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        // then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("인증 정보 추출: 토큰에서 인증 정보를 성공적으로 추출한다.")
    void getAuthentication_Success() {
        // given
        String token = jwtTokenProvider.generateToken(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(null);

        // when
        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        // then
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
    }
}
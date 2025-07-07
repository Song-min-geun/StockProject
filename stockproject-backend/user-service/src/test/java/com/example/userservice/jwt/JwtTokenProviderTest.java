package com.example.userservice.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider 순수 단위 테스트")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private final String secretKey = "V2VMaXZlSW5BVkNBdGl0eUFkdmFuY2VkQ29tcGFueUFjdGl2ZWx5RGV2ZWxvcGluZ1RTQUZvclRoZUZ1dHVyZQo=";
    private final long expirationTime = 3600000;
    private Key key;

    @BeforeEach
    void setUp() {
        // 생성자에 테스트용 값을 직접 주입하여 객체 생성
        jwtTokenProvider = new JwtTokenProvider(secretKey, expirationTime, null);

        // 토큰 검증을 위해 테스트에서도 key를 생성
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    @DisplayName("토큰 생성: 성공")
    void generateToken_Success() {
        // given
        String email = "test@example.com";

        // when
        String token = jwtTokenProvider.generateToken(email);

        // then
        assertThat(token).isNotNull();
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        assertThat(claims.getSubject()).isEqualTo(email);
    }

    @Test
    @DisplayName("토큰 유효성 검증: 성공")
    void validateToken_Success() {
        // given
        String email = "test@example.com";
        String token = jwtTokenProvider.generateToken(email);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("토큰 유효성 검증: 실패 - 만료된 토큰")
    void validateToken_Fail_Expired() {
        // given
        String expiredToken = Jwts.builder()
                .setSubject("test@example.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() - 1))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // when
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰에서 이메일 추출: 성공")
    void getEmailFromToken_Success() {
        // given
        String email = "user@google.com";
        String token = jwtTokenProvider.generateToken(email);

        // when
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        // then
        assertThat(extractedEmail).isEqualTo(email);
    }
}
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
    private String secretKey = "V2VMaXZlSW5BVkNBdGl0eUFkdmFuY2VkQ29tcGFueUFjdGl2ZWx5RGV2ZWxvcGluZ1RTQUZvclRoZUZ1dHVyZQo="; // 실제 키와 동일한 값
    private Key key;
    private long expirationTime = 3600000; // 1시간

    @BeforeEach
    void setUp() {
        // UserDetailsService 의존성을 제거하기 위해 null로 주입하고, 필요한 메소드만 직접 구현하여 테스트
        // 실제 프로젝트에서는 UserDetailsService를 Mocking하여 주입해야 합니다.
        // 여기서는 해당 의존성을 사용하는 getAuthentication 메소드를 테스트에서 분리합니다.
        jwtTokenProvider = new JwtTokenProvider(null); // UserDetailsService가 필요없는 메소드만 테스트

        // Reflection을 사용하거나, 테스트를 위해 setter를 열어주어 의존성을 주입합니다.
        // 여기서는 간단하게 필드 값을 직접 설정하는 방식을 보여주기 위해
        // 테스트용 생성자나 초기화 메소드가 있다고 가정합니다.
        // 실제로는 @Value 필드를 테스트에서 다루기 위해 TestPropertySource 등을 사용합니다.
        // 하지만 순수 단위 테스트를 위해 직접 값을 주입합니다.
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);

        // 테스트를 위해 임시로 `init` 메소드를 직접 호출하여 key를 초기화합니다.
        // JwtTokenProvider의 secretKey, expirationTime 필드를 public으로 바꾸거나 setter를 열어줘야 합니다.
        // 또는 아래와 같이 테스트용 클래스를 상속받아 만들 수 있습니다.
        // 여기서는 개념 설명을 위해 직접 초기화 코드를 작성합니다.
        // 실제 코드에서는 테스트 용이성을 위해 구조 변경을 고려해야 합니다.

        // 테스트 대상 객체 수동 생성 및 초기화
        jwtTokenProvider = new TestableJwtTokenProvider(null, secretKey, expirationTime);
        ((TestableJwtTokenProvider) jwtTokenProvider).initForTest();
    }

    // 테스트를 위해 JwtTokenProvider를 상속하고 초기화 메소드를 노출하는 내부 클래스
    private static class TestableJwtTokenProvider extends JwtTokenProvider {
        private final String secretKey;
        private final long expirationTime;

        public TestableJwtTokenProvider(org.springframework.security.core.userdetails.UserDetailsService userDetailsService, String secretKey, long expirationTime) {
            super(userDetailsService);
            this.secretKey = secretKey;
            this.expirationTime = expirationTime;
        }

        // 테스트에서만 호출될 초기화 메소드
        public void initForTest() {
            // @Value 필드를 수동으로 설정
            // (실제 코드에서는 Reflection 또는 생성자 주입을 통해 이 문제를 해결)
            super.secretKey = this.secretKey;
            super.expirationTime = this.expirationTime;
            super.init();
        }
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
        // 만료 시간을 0으로 설정하여 즉시 만료되는 토큰 생성
        String expiredToken = Jwts.builder()
                .setSubject("test@example.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() - 1)) // 과거 시간으로 만료 설정
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
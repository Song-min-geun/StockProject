package com.example.userservice.service;


import com.example.userservice.domin.User;
import com.example.userservice.dto.LoginRequestDto;
import com.example.userservice.dto.SignUpRequestDto;
import com.example.userservice.dto.TokenResponseDto;
import com.example.userservice.jwt.JwtTokenProvider;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName(" User 서비스 테스트 시작 ")
public class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName(" 회원가입 성공. ")
    void Sign_up_success(){
        //given
        SignUpRequestDto requestDto = new SignUpRequestDto("minkeon44@naver.com","password1234");
        //이메일이 중복되지 않음
        when(userRepository.findByEmail(requestDto.email())).thenReturn(Optional.empty());
        //비밀번호가 암호화됨
        when(passwordEncoder.encode(requestDto.password())).thenReturn("encodedPassword");
        //이후 이메일(ID)와 비밀번호가 저장
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when 회원가입 성공
        userService.signUp(requestDto);

        //then 아무일도 없으면 성공
    }

    @Test
    @DisplayName(" 회원가입 실패 이미 존재한 이메일")
    void sign_up_fail_same_email(){
        // given
        SignUpRequestDto requestDto = new SignUpRequestDto("test@example.com", "password123");
        // 이메일이 이미 존재한다고 가정
        when(userRepository.findByEmail(requestDto.email())).thenReturn(Optional.of(new User("test@example.com", "anypassword")));

        // when & then
        // signUp 메소드 호출 시 IllegalArgumentException이 발생하는지 검증
        assertThatThrownBy(() -> userService.signUp(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용중인 이메일입니다.");
    }

    @Test
    @DisplayName(" 로그인 성공")
    void login_success(){
        // given
        LoginRequestDto requestDto = new LoginRequestDto("test@example.com", "password123");
        User user = new User("test@example.com", "encodedPassword");
        // 이메일로 사용자를 찾았다고 가정
        when(userRepository.findByEmail(requestDto.email())).thenReturn(Optional.of(user));
        // 비밀번호가 일치한다고 가정
        when(passwordEncoder.matches(requestDto.password(), user.getPassword())).thenReturn(true);
        // 토큰이 정상적으로 생성된다고 가정
        when(jwtTokenProvider.generateToken(user.getEmail())).thenReturn("dummy-jwt-token");

        // when
        TokenResponseDto responseDto = userService.login(requestDto);

        // then
        // 반환된 토큰이 null이 아니고, 예상한 값과 일치하는지 검증
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.accessToken()).isEqualTo("dummy-jwt-token");
    }

    @Test
    @DisplayName("로그인: 실패 - 존재하지 않는 사용자")
    void login_Fail_UserNotFound() {
        // given
        LoginRequestDto requestDto = new LoginRequestDto("notfound@example.com", "password123");
        // 사용자를 찾을 수 없다고 가정
        when(userRepository.findByEmail(requestDto.email())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("로그인: 실패 - 비밀번호 불일치")
    void login_Fail_PasswordMismatch() {
        // given
        LoginRequestDto requestDto = new LoginRequestDto("test@example.com", "wrongpassword");
        User user = new User("test@example.com", "encodedPassword");
        when(userRepository.findByEmail(requestDto.email())).thenReturn(Optional.of(user));
        // 비밀번호가 일치하지 않는다고 가정
        when(passwordEncoder.matches(requestDto.password(), user.getPassword())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }
}

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("회원가입: 성공 - 새로운 사용자를 성공적으로 등록한다.")
    void signUp_Success() {
        // given
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword123";
        
        SignUpRequestDto requestDto = new SignUpRequestDto(email, password);
        User savedUser = new User(email, encodedPassword);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // when
        userService.signUp(requestDto);
        
        // then
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입: 실패 - 이미 사용 중인 이메일로 가입 시 예외가 발생한다.")
    void signUp_Fail_EmailAlreadyExists() {
        // given
        String email = "existing@example.com";
        String password = "password123";
        
        SignUpRequestDto requestDto = new SignUpRequestDto(email, password);
        User existingUser = new User(email, "encodedPassword123");
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        
        // when & then
        assertThatThrownBy(() -> userService.signUp(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용중인 이메일입니다.");
        
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인: 성공 - 올바른 이메일과 비밀번호로 로그인하면 토큰을 반환한다.")
    void login_Success() {
        // given
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword123";
        String token = "jwt.token.string";
        
        LoginRequestDto requestDto = new LoginRequestDto(email, password);
        User user = new User(email, encodedPassword);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtTokenProvider.generateToken(email)).thenReturn(token);
        
        // when
        TokenResponseDto responseDto = userService.login(requestDto);
        
        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.accessToken()).isEqualTo(token);
        
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, encodedPassword);
        verify(jwtTokenProvider).generateToken(email);
    }

    @Test
    @DisplayName("로그인: 실패 - 존재하지 않는 이메일로 로그인 시 예외가 발생한다.")
    void login_Fail_UserNotFound() {
        // given
        String email = "nonexistent@example.com";
        String password = "password123";
        
        LoginRequestDto requestDto = new LoginRequestDto(email, password);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> userService.login(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
        
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("로그인: 실패 - 잘못된 비밀번호로 로그인 시 예외가 발생한다.")
    void login_Fail_IncorrectPassword() {
        // given
        String email = "test@example.com";
        String password = "wrongPassword";
        String encodedPassword = "encodedPassword123";
        
        LoginRequestDto requestDto = new LoginRequestDto(email, password);
        User user = new User(email, encodedPassword);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);
        
        // when & then
        assertThatThrownBy(() -> userService.login(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
        
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, encodedPassword);
        verify(jwtTokenProvider, never()).generateToken(anyString());
    }
}
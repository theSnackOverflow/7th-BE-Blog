package com.example.blog.domain.auth;

import com.example.blog.domain.auth.client.KakaoOAuthClient;
import com.example.blog.domain.auth.dto.KakaoTokenResponse;
import com.example.blog.domain.auth.dto.KakaoUserInfo;
import com.example.blog.domain.auth.dto.LoginRequest;
import com.example.blog.domain.auth.dto.SignUpRequest;
import com.example.blog.domain.auth.dto.TokenResponse;
import com.example.blog.domain.auth.entity.RefreshToken;
import com.example.blog.domain.auth.repository.RefreshTokenRepository;
import com.example.blog.domain.auth.service.AuthService;
import com.example.blog.domain.user.entity.Provider;
import com.example.blog.domain.user.entity.User;
import com.example.blog.domain.user.repository.UserRepository;
import com.example.blog.global.exception.BusinessException;
import com.example.blog.global.exception.ErrorCode;
import com.example.blog.global.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private KakaoOAuthClient kakaoOAuthClient;

    @Mock
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshExpiration", 1209600000L);
        ReflectionTestUtils.setField(authService, "cookieSecure", false);
        lenient().when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
    }

    @Test
    void signUp_이메일_중복_예외() {
        given(userRepository.existsByEmailAndProvider("dup@example.com", Provider.LOCAL)).willReturn(true);

        SignUpRequest request = new SignUpRequest("dup@example.com", "pass123", "닉네임", null);

        assertThatThrownBy(() -> authService.signUp(request))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS));
    }

    @Test
    void signUp_닉네임_중복_예외() {
        given(userRepository.existsByEmailAndProvider(anyString(), any(Provider.class))).willReturn(false);
        given(userRepository.existsByUsername("중복닉네임")).willReturn(true);

        SignUpRequest request = new SignUpRequest("new@example.com", "pass123", "중복닉네임", null);

        assertThatThrownBy(() -> authService.signUp(request))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USERNAME_ALREADY_EXISTS));
    }

    @Test
    void login_사용자_없음_예외() {
        given(userRepository.findByEmailAndProvider("no@example.com", Provider.LOCAL)).willReturn(Optional.empty());

        LoginRequest request = new LoginRequest("no@example.com", "pass123");

        assertThatThrownBy(() -> authService.login(request, mock(HttpServletResponse.class)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    void login_비밀번호_불일치_예외() {
        User user = User.ofLocal("지훈", "jihoon@example.com", "hashedPw", null);
        given(userRepository.findByEmailAndProvider("jihoon@example.com", Provider.LOCAL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPw", "hashedPw")).willReturn(false);

        LoginRequest request = new LoginRequest("jihoon@example.com", "wrongPw");

        assertThatThrownBy(() -> authService.login(request, mock(HttpServletResponse.class)))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PASSWORD));
    }

    @Test
    void reissue_RT_없음_예외() {
        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
        given(request.getCookies()).willReturn(null);

        assertThatThrownBy(() -> authService.reissue(request))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    @Test
    void reissue_DB에_없는_RT_예외() {
        jakarta.servlet.http.Cookie cookie =
            new jakarta.servlet.http.Cookie("refreshToken", "validToken");
        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
        given(request.getCookies()).willReturn(new jakarta.servlet.http.Cookie[]{cookie});
        given(jwtUtil.validate("validToken")).willReturn(true);
        given(jwtUtil.getTokenType("validToken")).willReturn("REFRESH");
        given(refreshTokenRepository.findByToken("validToken")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.reissue(request))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
    }

    @Test
    void reissue_만료된_RT_예외() {
        jakarta.servlet.http.Cookie cookie =
            new jakarta.servlet.http.Cookie("refreshToken", "expiredToken");
        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
        given(request.getCookies()).willReturn(new jakarta.servlet.http.Cookie[]{cookie});
        given(jwtUtil.validate("expiredToken")).willReturn(true);
        given(jwtUtil.getTokenType("expiredToken")).willReturn("REFRESH");

        RefreshToken stored = RefreshToken.of(1L, "expiredToken", LocalDateTime.now().minusDays(1));
        given(refreshTokenRepository.findByToken("expiredToken")).willReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.reissue(request))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    @Test
    void kakaoCallback_기존유저_로그인() {
        KakaoTokenResponse kakaoToken = new KakaoTokenResponse("kakao_at", "kakao_rt", "bearer", 21599L);
        KakaoUserInfo userInfo = new KakaoUserInfo(
            12345L,
            new KakaoUserInfo.KakaoAccount("kakao@test.com",
                new KakaoUserInfo.KakaoAccount.Profile("테스트닉", null))
        );
        User existingUser = User.ofKakao("테스트닉", "kakao@test.com", null, "12345");

        given(kakaoOAuthClient.getToken("code123")).willReturn(kakaoToken);
        given(kakaoOAuthClient.getUserInfo("kakao_at")).willReturn(userInfo);
        given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "12345"))
            .willReturn(Optional.of(existingUser));
        given(jwtUtil.generateAccessToken(any(), anyString())).willReturn("access_token");
        given(jwtUtil.generateRefreshToken(any())).willReturn("refresh_token");
        given(refreshTokenRepository.findByUserId(any())).willReturn(Optional.empty());

        HttpServletResponse response = mock(HttpServletResponse.class);
        TokenResponse result = authService.kakaoCallback("code123", response);

        assertThat(result.accessToken()).isEqualTo("access_token");
    }

    @Test
    void kakaoCallback_신규유저_자동가입() {
        KakaoTokenResponse kakaoToken = new KakaoTokenResponse("kakao_at", "kakao_rt", "bearer", 21599L);
        KakaoUserInfo userInfo = new KakaoUserInfo(
            99999L,
            new KakaoUserInfo.KakaoAccount("new@kakao.com",
                new KakaoUserInfo.KakaoAccount.Profile("뉴유저", null))
        );
        User savedUser = User.ofKakao("뉴유저", "new@kakao.com", null, "99999");

        given(kakaoOAuthClient.getToken("newcode")).willReturn(kakaoToken);
        given(kakaoOAuthClient.getUserInfo("kakao_at")).willReturn(userInfo);
        given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "99999"))
            .willReturn(Optional.empty());
        given(userRepository.existsByUsername("뉴유저")).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(jwtUtil.generateAccessToken(any(), anyString())).willReturn("new_access_token");
        given(jwtUtil.generateRefreshToken(any())).willReturn("new_refresh_token");
        given(refreshTokenRepository.findByUserId(any())).willReturn(Optional.empty());

        HttpServletResponse response = mock(HttpServletResponse.class);
        TokenResponse result = authService.kakaoCallback("newcode", response);

        assertThat(result.accessToken()).isEqualTo("new_access_token");
    }

    @Test
    void kakaoCallback_username_중복시_suffix() {
        KakaoTokenResponse kakaoToken = new KakaoTokenResponse("kakao_at", "kakao_rt", "bearer", 21599L);
        KakaoUserInfo userInfo = new KakaoUserInfo(
            77777L,
            new KakaoUserInfo.KakaoAccount("dup@kakao.com",
                new KakaoUserInfo.KakaoAccount.Profile("중복닉", null))
        );
        User savedUser = User.ofKakao("중복닉_77777", "dup@kakao.com", null, "77777");

        given(kakaoOAuthClient.getToken("dupcode")).willReturn(kakaoToken);
        given(kakaoOAuthClient.getUserInfo("kakao_at")).willReturn(userInfo);
        given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "77777"))
            .willReturn(Optional.empty());
        given(userRepository.existsByUsername("중복닉")).willReturn(true);
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(jwtUtil.generateAccessToken(any(), anyString())).willReturn("at");
        given(jwtUtil.generateRefreshToken(any())).willReturn("rt");
        given(refreshTokenRepository.findByUserId(any())).willReturn(Optional.empty());

        HttpServletResponse response = mock(HttpServletResponse.class);
        TokenResponse result = authService.kakaoCallback("dupcode", response);

        assertThat(result.accessToken()).isEqualTo("at");
    }
}

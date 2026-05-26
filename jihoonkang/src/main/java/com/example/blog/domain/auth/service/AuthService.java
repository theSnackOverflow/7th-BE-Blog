package com.example.blog.domain.auth.service;

import com.example.blog.domain.auth.client.KakaoOAuthClient;
import com.example.blog.domain.auth.dto.KakaoTokenResponse;
import com.example.blog.domain.auth.dto.KakaoUserInfo;
import com.example.blog.domain.auth.dto.LoginRequest;
import com.example.blog.domain.auth.dto.SignUpRequest;
import com.example.blog.domain.auth.dto.TokenResponse;
import com.example.blog.domain.auth.entity.RefreshToken;
import com.example.blog.domain.auth.repository.RefreshTokenRepository;
import com.example.blog.domain.user.dto.UserResponse;
import com.example.blog.domain.user.entity.Provider;
import com.example.blog.domain.user.entity.User;
import com.example.blog.domain.user.repository.UserRepository;
import com.example.blog.global.exception.BusinessException;
import com.example.blog.global.exception.ErrorCode;
import com.example.blog.global.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final PlatformTransactionManager transactionManager;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${jwt.cookie.secure}")
    private boolean cookieSecure;

    @Transactional
    public UserResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmailAndProvider(request.email(), Provider.LOCAL)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        String hashedPassword = passwordEncoder.encode(request.password());
        User user = User.ofLocal(request.username(), request.email(), hashedPassword, request.profileUrl());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public TokenResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmailAndProvider(request.email(), Provider.LOCAL)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshExpiration / 1000);
        refreshTokenRepository.findByUserId(user.getId())
            .ifPresentOrElse(
                rt -> rt.update(refreshToken, expiresAt),
                () -> refreshTokenRepository.save(RefreshToken.of(user.getId(), refreshToken, expiresAt))
            );

        setRefreshTokenCookie(response, refreshToken);
        return new TokenResponse(accessToken);
    }

    @Transactional
    public TokenResponse reissue(HttpServletRequest request) {
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken == null || !jwtUtil.validate(refreshToken)
                || !"REFRESH".equals(jwtUtil.getTokenType(refreshToken))) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
        return new TokenResponse(newAccessToken);
    }

    public TokenResponse kakaoCallback(String code, HttpServletResponse response) {
        KakaoTokenResponse kakaoToken = kakaoOAuthClient.getToken(code);
        KakaoUserInfo userInfo = kakaoOAuthClient.getUserInfo(kakaoToken.accessToken());

        return new TransactionTemplate(transactionManager).execute(status -> {
            String providerId = String.valueOf(userInfo.id());

            User user = userRepository.findByProviderAndProviderId(Provider.KAKAO, providerId)
                .orElseGet(() -> registerKakaoUser(userInfo, providerId));

            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());

            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshExpiration / 1000);
            refreshTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                    rt -> rt.update(refreshToken, expiresAt),
                    () -> refreshTokenRepository.save(RefreshToken.of(user.getId(), refreshToken, expiresAt))
                );

            setRefreshTokenCookie(response, refreshToken);
            return new TokenResponse(accessToken);
        });
    }

    private User registerKakaoUser(KakaoUserInfo userInfo, String providerId) {
        String username = resolveUniqueUsername(userInfo.nickname(), providerId);
        String email = userInfo.email() != null ? userInfo.email() : "kakao_" + providerId + "@kakao.local";
        User newUser = User.ofKakao(username, email, userInfo.profileImageUrl(), providerId);
        return userRepository.save(newUser);
    }

    private String resolveUniqueUsername(String nickname, String providerId) {
        String candidate = nickname != null ? nickname : "kakao_" + providerId;
        if (!userRepository.existsByUsername(candidate)) {
            return candidate;
        }
        return candidate + "_" + providerId;
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(rt -> refreshTokenRepository.deleteByUserId(rt.getUserId()));
        }
        clearRefreshTokenCookie(response);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
            .httpOnly(true)
            .secure(cookieSecure)
            .sameSite("Lax")
            .path("/")
            .maxAge(0)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
            .httpOnly(true)
            .secure(cookieSecure)
            .sameSite("Lax")
            .path("/")
            .maxAge(refreshExpiration / 1000)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
            .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }
}

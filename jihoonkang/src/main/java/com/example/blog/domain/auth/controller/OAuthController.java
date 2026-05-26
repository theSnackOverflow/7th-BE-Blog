package com.example.blog.domain.auth.controller;

import com.example.blog.domain.auth.dto.TokenResponse;
import com.example.blog.domain.auth.service.AuthService;
import com.example.blog.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;


@Tag(name = "소셜 로그인", description = "카카오 OAuth 로그인")
@RestController
@RequestMapping("/oauth/kakao")
@RequiredArgsConstructor
public class OAuthController {

    private final AuthService authService;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.authorize-uri}")
    private String authorizeUri;

    @Value("${kakao.logout-uri}")
    private String logoutUri;

    @Value("${kakao.logout-redirect-uri}")
    private String logoutRedirectUri;

    @Operation(summary = "카카오 로그인 시작", description = "카카오 인가 페이지로 리다이렉트합니다.")
    @GetMapping("/login")
    public void redirectToKakao(HttpServletResponse response) throws java.io.IOException {
        String authUrl = UriComponentsBuilder.fromHttpUrl(authorizeUri)
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("response_type", "code")
            .toUriString();
        response.sendRedirect(authUrl);
    }

    @Operation(summary = "카카오 콜백", description = "인가코드로 자체 AT/RT를 발급합니다. AT는 응답 바디, RT는 HttpOnly 쿠키로 반환합니다.")
    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<TokenResponse>> kakaoCallback(@RequestParam String code,
                                                                     HttpServletResponse response) {
        TokenResponse tokenResponse = authService.kakaoCallback(code, response);
        return ResponseEntity.ok(ApiResponse.success(tokenResponse));
    }

    @Operation(summary = "카카오 로그아웃 시작", description = "카카오 로그아웃 페이지로 리다이렉트합니다.")
    @GetMapping("/logout/start")
    public void startLogout(HttpServletResponse response) throws java.io.IOException {
        String kakaoLogoutUrl = UriComponentsBuilder.fromHttpUrl(logoutUri)
            .queryParam("client_id", clientId)
            .queryParam("logout_redirect_uri", logoutRedirectUri)
            .toUriString();
        response.sendRedirect(kakaoLogoutUrl);
    }

    @Operation(summary = "카카오 로그아웃 콜백", description = "카카오 로그아웃 후 로컬 RT를 삭제하고 쿠키를 초기화합니다.")
    @GetMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request,
                                                    HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

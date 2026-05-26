package com.example.blog.domain.auth.controller;

import com.example.blog.domain.auth.dto.LoginRequest;
import com.example.blog.domain.auth.dto.SignUpRequest;
import com.example.blog.domain.auth.dto.TokenResponse;
import com.example.blog.domain.auth.service.AuthService;
import com.example.blog.domain.user.dto.UserResponse;
import com.example.blog.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "회원가입, 로그인, 토큰 재발급")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "이메일 회원가입", description = "이메일/비밀번호/닉네임으로 신규 가입합니다.")
    @PostMapping("/auth")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> signUp(@RequestBody @Valid SignUpRequest request) {
        return ApiResponse.success(authService.signUp(request));
    }

    @Operation(summary = "이메일 로그인", description = "Access Token 바디 반환, Refresh Token HttpOnly Cookie 설정")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody @Valid LoginRequest request,
                                             HttpServletResponse response) {
        return ApiResponse.success(authService.login(request, response));
    }

    @Operation(summary = "Access Token 재발급", description = "Refresh Token Cookie로 새 Access Token 발급")
    @PostMapping("/reissue")
    public ApiResponse<TokenResponse> reissue(HttpServletRequest request) {
        return ApiResponse.success(authService.reissue(request));
    }
}

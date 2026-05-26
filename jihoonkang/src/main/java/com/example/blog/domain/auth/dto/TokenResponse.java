package com.example.blog.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 응답 (Refresh Token은 HttpOnly Cookie로 전달)")
public record TokenResponse(

    @Schema(description = "Access Token (Bearer)", example = "eyJhbGci...")
    String accessToken

) {}

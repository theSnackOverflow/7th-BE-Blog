package com.example.blog.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfo(
    Long id,
    @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
        String email,
        Profile profile
    ) {
        public record Profile(
            String nickname,
            @JsonProperty("profile_image_url") String profileImageUrl
        ) {}
    }

    public String email() {
        return kakaoAccount != null ? kakaoAccount.email() : null;
    }

    public String nickname() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().nickname();
    }

    public String profileImageUrl() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().profileImageUrl();
    }
}

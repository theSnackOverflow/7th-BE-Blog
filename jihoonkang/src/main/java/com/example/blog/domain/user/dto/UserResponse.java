package com.example.blog.domain.user.dto;

import com.example.blog.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "사용자 응답")
public record UserResponse(
    @Schema(description = "사용자 ID", example = "1") Long userId,
    @Schema(description = "표시 이름", example = "지훈") String username,
    @Schema(description = "이메일", example = "jihoon@example.com") String email,
    @Schema(description = "프로필 이미지 URL", nullable = true, example = "https://cdn.example.com/u/1.png") String profileUrl,
    @Schema(description = "생성 시각", example = "2026-05-04T10:30:00") LocalDateTime createdAt,
    @Schema(description = "수정 시각", example = "2026-05-04T10:30:00") LocalDateTime updatedAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getProfileUrl(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}

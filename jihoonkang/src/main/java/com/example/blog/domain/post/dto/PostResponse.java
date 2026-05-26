package com.example.blog.domain.post.dto;

import com.example.blog.domain.post.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "게시글 응답")
public record PostResponse(
    @Schema(description = "게시글 ID", example = "1") Long postId,
    @Schema(description = "작성자 ID", example = "1") Long userId,
    @Schema(description = "작성자 이름", example = "지훈") String username,
    @Schema(description = "제목", example = "Spring Boot로 블로그 만들기") String title,
    @Schema(description = "본문", example = "오늘은 Swagger를 적용해보았습니다.") String content,
    @Schema(description = "게시 상태", example = "PUBLISHED", allowableValues = {"PUBLISHED", "HIDDEN"}) String status,
    @Schema(description = "생성 시각", example = "2026-05-04T10:30:00") LocalDateTime createdAt,
    @Schema(description = "수정 시각", example = "2026-05-04T10:30:00") LocalDateTime updatedAt
) {

    public static PostResponse from(Post post) {
        return new PostResponse(
            post.getId(),
            post.getUser().getId(),
            post.getUser().getUsername(),
            post.getTitle(),
            post.getContent(),
            post.getStatus().name(),
            post.getCreatedAt(),
            post.getUpdatedAt()
        );
    }
}

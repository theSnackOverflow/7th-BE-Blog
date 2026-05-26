package com.example.blog.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "게시글 목록 응답 (페이지네이션)")
public record PostListResponse(
    @Schema(description = "게시글 목록") List<PostResponse> items,
    @Schema(description = "현재 페이지 (0부터 시작)", example = "0") int page,
    @Schema(description = "페이지 크기", example = "10") int size,
    @Schema(description = "전체 게시글 수", example = "42") long totalElements
) {}

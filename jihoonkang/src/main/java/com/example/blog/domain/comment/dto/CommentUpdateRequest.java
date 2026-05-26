package com.example.blog.domain.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "댓글 수정 요청")
public record CommentUpdateRequest(

    @Schema(description = "수정할 댓글 내용", example = "수정된 댓글 내용입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "댓글 내용은 필수입니다.")
    String content

) {}

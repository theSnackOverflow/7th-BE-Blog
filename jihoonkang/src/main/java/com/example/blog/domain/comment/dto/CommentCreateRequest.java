package com.example.blog.domain.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "댓글 작성 요청")
public record CommentCreateRequest(

    @Schema(description = "댓글 내용", example = "좋은 글이네요!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "댓글 내용은 필수입니다.")
    String content,

    @Schema(description = "부모 댓글 ID (대댓글 작성 시 지정)", nullable = true, example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Long parentCommentId

) {}

package com.example.blog.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 수정 요청 (변경할 필드만 포함)")
public record PostUpdateRequest(

    @Schema(description = "수정할 제목", example = "수정된 제목", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 200)
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    String title,

    @Schema(description = "수정할 본문", example = "수정된 본문 내용", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String content,

    @Schema(description = "수정할 게시 상태", example = "HIDDEN", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = {"PUBLISHED", "HIDDEN"})
    String status

) {}

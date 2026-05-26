package com.example.blog.domain.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "댓글 신고 요청")
public record ReportCommentRequest(
    @Schema(description = "신고 사유", example = "욕설이 포함된 댓글입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "신고 사유는 필수입니다.")
    String reason
) {}

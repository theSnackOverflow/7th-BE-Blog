package com.example.blog.domain.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "게시글 신고 요청")
public record ReportPostRequest(
    @Schema(description = "신고 사유", example = "부적절한 광고 게시글입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "신고 사유는 필수입니다.")
    String reason
) {}

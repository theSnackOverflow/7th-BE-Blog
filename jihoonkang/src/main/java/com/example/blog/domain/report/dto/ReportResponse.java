package com.example.blog.domain.report.dto;

import com.example.blog.domain.report.entity.Report;
import com.example.blog.domain.report.entity.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "신고 응답")
public record ReportResponse(
    @Schema(description = "신고 ID", example = "1") Long reportId,
    @Schema(description = "신고자 ID", example = "2") Long reporterId,
    @Schema(description = "신고자 이름", example = "홍길동") String reporterUsername,
    @Schema(description = "신고 대상 타입", example = "POST", allowableValues = {"POST", "COMMENT"}) String targetType,
    @Schema(description = "신고 대상 ID", example = "1") Long targetId,
    @Schema(description = "신고 사유", example = "부적절한 광고 게시글입니다.") String reason,
    @Schema(description = "신고 처리 상태", example = "PENDING", allowableValues = {"PENDING", "RESOLVED"}) ReportStatus status,
    @Schema(description = "처리 완료 시각 (PENDING이면 null)", nullable = true, example = "2026-05-04T11:00:00") LocalDateTime resolvedAt,
    @Schema(description = "신고 접수 시각", example = "2026-05-04T10:30:00") LocalDateTime createdAt
) {

    public static ReportResponse from(Report report) {
        String targetType = report.getPost() != null ? "POST" : "COMMENT";
        Long targetId = report.getPost() != null
            ? report.getPost().getId()
            : report.getComment().getId();
        return new ReportResponse(
            report.getId(),
            report.getReporter().getId(),
            report.getReporter().getUsername(),
            targetType,
            targetId,
            report.getReason(),
            report.getStatus(),
            report.getResolvedAt(),
            report.getCreatedAt()
        );
    }
}

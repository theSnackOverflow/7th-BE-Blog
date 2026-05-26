package com.example.blog.domain.report.controller;

import com.example.blog.domain.report.dto.ReportResponse;
import com.example.blog.domain.report.service.ReportService;
import com.example.blog.global.response.ApiResponse;
import com.example.blog.global.response.ErrorApiResponse;
import com.example.blog.global.swagger.CommonErrorResponses;
import com.example.blog.global.swagger.ConflictReportResolved;
import com.example.blog.global.swagger.ForbiddenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "신고 관리", description = "신고 처리 (관리자 전용)")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "신고 처리", description = "PENDING 상태의 신고를 RESOLVED로 처리합니다. 관리자 전용.")
    @CommonErrorResponses
    @ForbiddenResponse
    @ConflictReportResolved
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "신고를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorApiResponse.class))
        )
    })
    @PostMapping("/{reportId}/resolve")
    public ApiResponse<ReportResponse> resolve(
        @AuthenticationPrincipal Long handlerId,
        @Parameter(description = "신고 ID", example = "1") @PathVariable Long reportId
    ) {
        return ApiResponse.success(reportService.resolveReport(handlerId, reportId));
    }

    @Operation(summary = "신고 목록 조회", description = "전체 신고 목록을 조회합니다. 관리자 전용.")
    @CommonErrorResponses
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ApiResponse<List<ReportResponse>> findAll() {
        return ApiResponse.success(reportService.findAll());
    }
}

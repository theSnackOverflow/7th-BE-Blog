package com.example.blog.domain.comment.controller;

import com.example.blog.domain.comment.dto.CommentCreateRequest;
import com.example.blog.domain.comment.dto.CommentResponse;
import com.example.blog.domain.comment.dto.CommentUpdateRequest;
import com.example.blog.domain.comment.service.CommentService;
import com.example.blog.domain.report.dto.ReportCommentRequest;
import com.example.blog.domain.report.dto.ReportResponse;
import com.example.blog.domain.report.service.ReportService;
import com.example.blog.global.response.ApiResponse;
import com.example.blog.global.response.ErrorApiResponse;
import com.example.blog.global.swagger.CommonErrorResponses;
import com.example.blog.global.swagger.ConflictAlreadyReported;
import com.example.blog.global.swagger.ForbiddenResponse;
import com.example.blog.global.swagger.NotFoundCommentResponse;
import com.example.blog.global.swagger.NotFoundPostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "댓글", description = "댓글 CRUD, 채택, 신고")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final ReportService reportService;

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다. parentCommentId를 지정하면 대댓글이 됩니다.")
    @CommonErrorResponses
    @NotFoundPostResponse
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "댓글 작성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 부모 댓글 (C002)",
            content = @Content(
                schema = @Schema(implementation = ErrorApiResponse.class),
                examples = @ExampleObject(value = "{\"status\":\"error\",\"message\":\"유효하지 않은 부모 댓글입니다.\",\"data\":null}")
            )
        )
    })
    @PostMapping("/api/v1/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> create(
        @AuthenticationPrincipal Long userId,
        @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
        @RequestBody @Valid CommentCreateRequest request
    ) {
        return ApiResponse.success(commentService.create(userId, postId, request));
    }

    @Operation(summary = "게시글의 댓글 목록 조회")
    @CommonErrorResponses
    @NotFoundPostResponse
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/api/v1/posts/{postId}/comments")
    public ApiResponse<List<CommentResponse>> findByPostId(
        @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId
    ) {
        return ApiResponse.success(commentService.findByPostId(postId));
    }

    @Operation(summary = "댓글 수정")
    @CommonErrorResponses
    @ForbiddenResponse
    @NotFoundCommentResponse
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공")
    })
    @PatchMapping("/api/v1/comments/{commentId}")
    public ApiResponse<CommentResponse> update(
        @AuthenticationPrincipal Long userId,
        @Parameter(description = "댓글 ID", example = "1") @PathVariable Long commentId,
        @RequestBody @Valid CommentUpdateRequest request
    ) {
        return ApiResponse.success(commentService.update(userId, commentId, request));
    }

    @Operation(summary = "댓글 삭제")
    @CommonErrorResponses
    @ForbiddenResponse
    @NotFoundCommentResponse
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content)
    })
    @DeleteMapping("/api/v1/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @AuthenticationPrincipal Long userId,
        @Parameter(description = "댓글 ID", example = "1") @PathVariable Long commentId
    ) {
        commentService.delete(userId, commentId);
    }

    @Operation(summary = "댓글 채택", description = "게시글 작성자가 댓글을 채택합니다. 게시글당 하나의 댓글만 채택 가능합니다.")
    @CommonErrorResponses
    @ForbiddenResponse
    @NotFoundPostResponse
    @NotFoundCommentResponse
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "채택 성공")
    })
    @PostMapping("/api/v1/posts/{postId}/comments/{commentId}/accept")
    public ApiResponse<CommentResponse> accept(
        @AuthenticationPrincipal Long userId,
        @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
        @Parameter(description = "채택할 댓글 ID", example = "1") @PathVariable Long commentId
    ) {
        return ApiResponse.success(commentService.acceptComment(userId, postId, commentId));
    }

    @Operation(summary = "댓글 신고", description = "댓글을 신고합니다. 자신의 댓글 신고 불가(R003), 중복 신고 불가(R002).")
    @CommonErrorResponses
    @ConflictAlreadyReported
    @NotFoundCommentResponse
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "신고 접수 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 신고 대상(R001) 또는 자신의 댓글 신고(R003)",
            content = @Content(
                schema = @Schema(implementation = ErrorApiResponse.class),
                examples = {
                    @ExampleObject(name = "R001", value = "{\"status\":\"error\",\"message\":\"유효하지 않은 신고 대상입니다.\",\"data\":null}"),
                    @ExampleObject(name = "R003", value = "{\"status\":\"error\",\"message\":\"자신의 게시물/댓글은 신고할 수 없습니다.\",\"data\":null}")
                }
            )
        )
    })
    @PostMapping("/api/v1/comments/{commentId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReportResponse> reportComment(
        @AuthenticationPrincipal Long reporterId,
        @Parameter(description = "댓글 ID", example = "1") @PathVariable Long commentId,
        @RequestBody @Valid ReportCommentRequest request
    ) {
        return ApiResponse.success(reportService.reportComment(reporterId, commentId, request));
    }
}

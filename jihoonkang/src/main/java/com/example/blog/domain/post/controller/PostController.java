package com.example.blog.domain.post.controller;

import com.example.blog.domain.post.dto.PostCreateRequest;
import com.example.blog.domain.post.dto.PostListResponse;
import com.example.blog.domain.post.dto.PostResponse;
import com.example.blog.domain.post.dto.PostUpdateRequest;
import com.example.blog.domain.post.service.PostService;
import com.example.blog.domain.report.dto.ReportPostRequest;
import com.example.blog.domain.report.dto.ReportResponse;
import com.example.blog.domain.report.service.ReportService;
import com.example.blog.global.response.ApiResponse;
import com.example.blog.global.response.ErrorApiResponse;
import com.example.blog.global.swagger.CommonErrorResponses;
import com.example.blog.global.swagger.ConflictAlreadyHidden;
import com.example.blog.global.swagger.ConflictAlreadyReported;
import com.example.blog.global.swagger.ForbiddenResponse;
import com.example.blog.global.swagger.NotFoundPostResponse;
import com.example.blog.global.swagger.NotFoundUserResponse;
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

@Tag(name = "게시글", description = "게시글 CRUD, 숨김, 신고")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final ReportService reportService;

    @Operation(summary = "게시글 작성")
    @CommonErrorResponses
    @NotFoundUserResponse
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "작성 성공")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> create(
        @AuthenticationPrincipal Long userId,
        @RequestBody @Valid PostCreateRequest request
    ) {
        return ApiResponse.success(postService.create(userId, request));
    }

    @Operation(
        summary = "게시글 목록 조회",
        parameters = {
            @Parameter(name = "status", description = "필터링할 상태. 미지정 시 전체 조회", example = "PUBLISHED",
                schema = @Schema(allowableValues = {"PUBLISHED", "HIDDEN"})),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "페이지 크기", example = "10")
        }
    )
    @CommonErrorResponses
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ApiResponse<PostListResponse> findAll(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(postService.findAll(status, page, size));
    }

    @Operation(summary = "게시글 단건 조회")
    @CommonErrorResponses
    @NotFoundPostResponse
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> findById(
        @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId
    ) {
        return ApiResponse.success(postService.findById(postId));
    }

    @Operation(summary = "게시글 수정")
    @CommonErrorResponses
    @ForbiddenResponse
    @NotFoundPostResponse
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공")
    })
    @PatchMapping("/{postId}")
    public ApiResponse<PostResponse> update(
        @AuthenticationPrincipal Long userId,
        @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
        @RequestBody @Valid PostUpdateRequest request
    ) {
        return ApiResponse.success(postService.update(userId, postId, request));
    }

    @Operation(summary = "게시글 삭제")
    @CommonErrorResponses
    @ForbiddenResponse
    @NotFoundPostResponse
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content)
    })
    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @AuthenticationPrincipal Long userId,
        @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId
    ) {
        postService.delete(userId, postId);
    }

    @Operation(summary = "게시글 숨김", description = "PUBLISHED 상태의 게시글을 HIDDEN으로 전환합니다. 이미 숨김 처리된 경우 409.")
    @CommonErrorResponses
    @ForbiddenResponse
    @NotFoundPostResponse
    @ConflictAlreadyHidden
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "숨김 성공")
    })
    @PostMapping("/{postId}/hide")
    public ApiResponse<PostResponse> hide(
        @AuthenticationPrincipal Long userId,
        @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId
    ) {
        return ApiResponse.success(postService.hidePost(userId, postId));
    }

    @Operation(summary = "게시글 신고", description = "게시글을 신고합니다. 자신의 게시글 신고 불가(R003), 중복 신고 불가(R002).")
    @CommonErrorResponses
    @ConflictAlreadyReported
    @NotFoundPostResponse
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "신고 접수 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 신고 대상(R001) 또는 자신의 게시글 신고(R003)",
            content = @Content(
                schema = @Schema(implementation = ErrorApiResponse.class),
                examples = {
                    @ExampleObject(name = "R001", value = "{\"status\":\"error\",\"message\":\"유효하지 않은 신고 대상입니다.\",\"data\":null}"),
                    @ExampleObject(name = "R003", value = "{\"status\":\"error\",\"message\":\"자신의 게시물/댓글은 신고할 수 없습니다.\",\"data\":null}")
                }
            )
        )
    })
    @PostMapping("/{postId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReportResponse> reportPost(
        @AuthenticationPrincipal Long reporterId,
        @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
        @RequestBody @Valid ReportPostRequest request
    ) {
        return ApiResponse.success(reportService.reportPost(reporterId, postId, request));
    }
}

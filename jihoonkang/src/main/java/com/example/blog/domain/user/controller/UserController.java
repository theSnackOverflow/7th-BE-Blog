package com.example.blog.domain.user.controller;

import com.example.blog.domain.user.dto.UserCreateRequest;
import com.example.blog.domain.user.dto.UserResponse;
import com.example.blog.domain.user.dto.UserUpdateRequest;
import com.example.blog.domain.user.service.UserService;
import com.example.blog.global.response.ApiResponse;
import com.example.blog.global.response.ErrorApiResponse;
import com.example.blog.global.swagger.CommonErrorResponses;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자", description = "사용자 가입/조회/수정/탈퇴")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 가입", description = "이메일/비밀번호/이름으로 신규 가입합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "가입 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "이메일 중복 (U002)",
            content = @Content(
                schema = @Schema(implementation = ErrorApiResponse.class),
                examples = @ExampleObject(value = "{\"status\":\"error\",\"message\":\"이미 사용 중인 이메일입니다.\",\"data\":null}")
            )
        )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> create(@RequestBody @Valid UserCreateRequest request) {
        return ApiResponse.success(userService.create(request));
    }

    @Operation(summary = "사용자 단건 조회")
    @CommonErrorResponses
    @NotFoundUserResponse
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> findById(
        @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId
    ) {
        return ApiResponse.success(userService.findById(userId));
    }

    @Operation(summary = "사용자 정보 수정")
    @CommonErrorResponses
    @NotFoundUserResponse
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공")
    })
    @PatchMapping("/{userId}")
    public ApiResponse<UserResponse> update(
        @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId,
        @RequestBody @Valid UserUpdateRequest request
    ) {
        return ApiResponse.success(userService.update(userId, request));
    }

    @Operation(summary = "사용자 탈퇴")
    @CommonErrorResponses
    @NotFoundUserResponse
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "탈퇴 성공", content = @Content)
    })
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId
    ) {
        userService.delete(userId);
    }
}

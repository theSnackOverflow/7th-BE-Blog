package com.example.blog.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "사용자 정보 수정 요청 (변경할 필드만 포함)")
public record UserUpdateRequest(

    @Schema(description = "수정할 이름", example = "지훈변경", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 50)
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    String username,

    @Schema(description = "수정할 프로필 이미지 URL", example = "https://cdn.example.com/u/2.png", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 200)
    @Size(max = 200, message = "프로필 URL은 200자 이하여야 합니다.")
    String profileUrl

) {}

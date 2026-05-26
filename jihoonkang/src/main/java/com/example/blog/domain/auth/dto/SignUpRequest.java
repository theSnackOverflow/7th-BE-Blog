package com.example.blog.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "이메일 회원가입 요청")
public record SignUpRequest(

    @Schema(description = "이메일", example = "jihoon@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
    String email,

    @Schema(description = "비밀번호", example = "P@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 6, max = 100, message = "비밀번호는 6자 이상 100자 이하여야 합니다.")
    String password,

    @Schema(description = "닉네임", example = "지훈", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
    String username,

    @Schema(description = "프로필 이미지 URL (선택)", example = "https://cdn.example.com/u/1.png", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 200, message = "프로필 URL은 200자 이하여야 합니다.")
    String profileUrl

) {}

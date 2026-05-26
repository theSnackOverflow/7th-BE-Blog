package com.example.blog.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 작성 요청")
public record PostCreateRequest(

    @Schema(description = "제목", example = "Spring Boot로 블로그 만들기", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 200)
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    String title,

    @Schema(description = "본문", example = "오늘은 Spring Boot 3.x와 springdoc으로 Swagger를 적용해보았습니다.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String content,

    @Schema(description = "게시 상태 (미지정 시 PUBLISHED)", example = "PUBLISHED", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = {"PUBLISHED", "HIDDEN"})
    String status

) {}

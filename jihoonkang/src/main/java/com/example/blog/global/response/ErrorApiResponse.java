package com.example.blog.global.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "에러 응답 래퍼 (status=error, data=null)",
    example = "{\"status\":\"error\",\"message\":\"오류 메시지\",\"data\":null}"
)
public class ErrorApiResponse {

    @Schema(description = "처리 상태", example = "error")
    private String status;

    @Schema(description = "오류 메시지", example = "사용자를 찾을 수 없습니다.")
    private String message;

    @Schema(description = "응답 데이터 (항상 null)", nullable = true, example = "null")
    private Object data;
}

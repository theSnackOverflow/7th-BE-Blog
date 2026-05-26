package com.example.blog.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "공통 응답 래퍼")
public class ApiResponse<T> {

    @Schema(description = "처리 상태", example = "success", allowableValues = {"success", "error"})
    private String status;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private String message;

    @Schema(description = "응답 데이터 (status=error 시 null)")
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", "요청이 성공적으로 처리되었습니다.", data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null);
    }
}

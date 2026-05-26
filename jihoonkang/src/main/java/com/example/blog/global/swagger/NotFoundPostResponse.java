package com.example.blog.global.swagger;

import com.example.blog.global.response.ErrorApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
    responseCode = "404",
    description = "게시글을 찾을 수 없음 (P001)",
    content = @Content(
        schema = @Schema(implementation = ErrorApiResponse.class),
        examples = @ExampleObject(value = "{\"status\":\"error\",\"message\":\"게시글을 찾을 수 없습니다.\",\"data\":null}")
    )
)
public @interface NotFoundPostResponse {
}

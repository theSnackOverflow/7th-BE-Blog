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
    responseCode = "409",
    description = "이미 신고한 대상 (R002)",
    content = @Content(
        schema = @Schema(implementation = ErrorApiResponse.class),
        examples = @ExampleObject(value = "{\"status\":\"error\",\"message\":\"이미 신고한 대상입니다.\",\"data\":null}")
    )
)
public @interface ConflictAlreadyReported {
}

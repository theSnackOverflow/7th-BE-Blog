package com.example.blog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "헬스체크", description = "서버 가용성 확인")
@RestController
public class HealthController {

    @Operation(summary = "헬스체크", description = "서버가 정상 동작 중인지 확인합니다. 인증 불필요.")
    @SecurityRequirements
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "서버 정상",
            content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "ok"))
        )
    })
    @GetMapping("/api/v1/health")
    public String health() {
        return "ok";
    }
}

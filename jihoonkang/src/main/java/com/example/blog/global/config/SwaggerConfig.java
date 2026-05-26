package com.example.blog.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI blogOpenAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local 개발 서버")
            ))
            .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, apiKeySecurityScheme()))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    private Info apiInfo() {
        return new Info()
            .title("Leets 7기 BE 블로그 API")
            .description("""
                블로그 도메인 REST API 명세서.
                - 인증: Bearer JWT (우상단 Authorize 버튼에 Access Token 입력)
                - 응답 형식: ApiResponse<T> 래퍼 (status / message / data)
                - 에러 코드: AUTH_001~006, U001~U003, P001/P002, C001/C002, A001, R001~R004
                """)
            .version("v1.0.0")
            .contact(new Contact()
                .name("jihoonkang")
                .email("ivory.ma9ic@gmail.com"))
            .license(new License().name("MIT").url("https://opensource.org/licenses/MIT"));
    }

    private SecurityScheme apiKeySecurityScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("로그인 후 발급받은 Access Token을 입력하세요 (Bearer 접두어 제외)");
    }
}

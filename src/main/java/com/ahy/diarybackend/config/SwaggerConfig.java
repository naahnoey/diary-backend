package com.ahy.diarybackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Diary API",
                version = "v1",
                description = "Spring Boot를 활용한 다이어리 프로젝트의 Swagger API 문서"
        )
)
public class SwaggerConfig {
}
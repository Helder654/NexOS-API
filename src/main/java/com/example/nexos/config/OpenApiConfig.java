package com.example.nexos.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "NexOS API",
        version = "v1",
        description = "API REST para gerenciamento de assistência técnica."))
public class OpenApiConfig {
}

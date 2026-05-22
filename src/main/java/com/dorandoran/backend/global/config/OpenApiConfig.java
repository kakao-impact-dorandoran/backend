package com.dorandoran.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "BearerAuth";
    private static final String DEVICE_SCHEME = "DeviceTokenAuth";

    @Bean
    public OpenAPI dorandoranOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("도란도란 API")
                        .description("도란도란 — 카카오 테크포임팩트 x 안무서운회사 백엔드 API")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization"))
                        .addSecuritySchemes(DEVICE_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("어르신 전용 태블릿 인증: Authorization: Device {deviceToken}")));
    }
}

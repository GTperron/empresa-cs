package com.empresa.inventario.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI/Swagger.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Empresa CS - API de Gestión de Inventario")
                        .version("1.0.0")
                        .description("API Backend para gestión de inventario/stock de negocio genérico")
                        .contact(new Contact()
                                .name("Empresa CS")
                                .email("support@empresa.com")
                                .url("https://empresa.com")
                        )
                )
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Bearer Token")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}

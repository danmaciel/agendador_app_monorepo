package com.danmaciel.agendador_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                Map<String, Schema> schemas = new HashMap<>();

                Schema timeSchema = new Schema<>();
                timeSchema.setType("string");
                timeSchema.setFormat("HH:mm");
                timeSchema.setExample("14:00");
                schemas.put("LocalTime", timeSchema);

                Schema dateSchema = new Schema<>();
                dateSchema.setType("string");
                dateSchema.setFormat("date");
                dateSchema.setExample("2026-03-20");
                schemas.put("LocalDate", dateSchema);

                return new OpenAPI()
                                .info(new Info()
                                                .title("Agendador API")
                                                .description("API para sistema de agendamento de serviços")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("danmaciel")
                                                                .email("dan.beta2010@gmail.com")))
                                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                                .components(new Components()
                                                .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description("JWT token de autenticação"))
                                                .schemas(schemas));
        }
}

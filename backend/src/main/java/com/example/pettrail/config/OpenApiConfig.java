package com.example.pettrail.config;

import com.example.pettrail.dto.ErrorResponse;
import com.example.pettrail.dto.ValidationError;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PatTrail API")
                        .description("A REST API for pet walk tracking and management. Features include pet management, walk tracking with GPS coordinates, and comprehensive error handling.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PatTrail Team")
                                .email("contato@pattrail.com")
                                .url("https://pattrail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.pattrail.com")
                                .description("Production Server")
                ))
                .components(new Components()
                        .addSchemas("ErrorDetail", new Schema<ValidationError>()
                                .type("object")
                                .addProperty("field", new StringSchema().description("Field name that failed validation"))
                                .addProperty("issue", new StringSchema().description("Description of the validation issue")))
                        .addSchemas("ErrorResponse", new Schema<ErrorResponse>()
                                .type("object")
                                .required(List.of("code", "message"))
                                .addProperty("code", new StringSchema()
                                        .description("Error code")
                                        ._enum(List.of("VALIDATION_ERROR", "NOT_FOUND", "CONFLICT", "INTERNAL_ERROR")))
                                .addProperty("message", new StringSchema().description("Human-readable error message"))
                                .addProperty("details", new ArraySchema()
                                        .description("Optional array with field-level validation issues")
                                        .items(new Schema<>().$ref("#/components/schemas/ErrorDetail")))));
    }
}

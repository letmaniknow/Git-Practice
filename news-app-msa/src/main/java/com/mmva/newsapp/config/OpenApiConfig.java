package com.mmva.newsapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info().title("The News API")
                                                .version("1.0")
                                                .description(
                                                                "API documentation for The News application. All responses, including errors, are wrapped in ApiResponseDto<T> for consistency."));
        }

        @Bean
        public OpenApiCustomizer globalResponseCustomizer() {
                return openApi -> openApi.getPaths().values()
                                .forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                                        ApiResponses responses = operation.getResponses();
                                        // Add a global error response example for 400 and 500
                                        ApiResponse errorResponse = new ApiResponse()
                                                        .description("Error response")
                                                        .content(new Content().addMediaType("application/json",
                                                                        new MediaType().schema(new Schema<>()
                                                                                        .$ref("#/components/schemas/ApiResponseDtoErrorResponse"))));
                                        responses.addApiResponse("400", errorResponse);
                                        responses.addApiResponse("500", errorResponse);
                                }));
        }

        @Bean
        public OpenApiCustomizer addErrorResponseSchema() {
                return openApi -> {
                        Components components = openApi.getComponents();
                        // ErrorResponse schema with RFC 7807 compliance
                        Schema<?> errorResponseSchema = new ObjectSchema()
                                        .addProperty("timestamp", new StringSchema().format("date-time"))
                                        .addProperty("status", new IntegerSchema())
                                        .addProperty("error", new StringSchema())
                                        .addProperty("code", new StringSchema())
                                        .addProperty("message", new StringSchema())
                                        .addProperty("path", new StringSchema())
                                        .addProperty("fieldErrors", new MapSchema()
                                                        .additionalProperties(new StringSchema())
                                                        .description("Field-level validation errors (optional)"));

                        // ApiResponseDto<ErrorResponse> schema
                        Schema<?> apiResponseDtoErrorResponseSchema = new ObjectSchema()
                                        .addProperty("status", new StringSchema())
                                        .addProperty("message", new StringSchema())
                                        .addProperty("timestamp", new StringSchema().format("date-time"))
                                        .addProperty("data", errorResponseSchema);
                        components.addSchemas("ErrorResponse", errorResponseSchema);
                        components.addSchemas("ApiResponseDtoErrorResponse", apiResponseDtoErrorResponseSchema);
                };
        }
}

package com.mmva.newsapp.infrastructure.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson configuration to ensure proper JSON serialization.
 * Prevents URL encoding of string values in JSON responses.
 */
@Configuration
public class JsonSerializationConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS)
                .featuresToDisable(com.fasterxml.jackson.core.JsonGenerator.Feature.ESCAPE_NON_ASCII)
                .build();
    }
}
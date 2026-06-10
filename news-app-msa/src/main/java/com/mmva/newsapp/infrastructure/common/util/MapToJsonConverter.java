package com.mmva.newsapp.infrastructure.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA AttributeConverter for converting Map&lt;String, String&gt; to/from JSON
 * string.
 * 
 * <p>
 * This converter provides cross-database compatibility by storing JSON data as
 * a string
 * (NVARCHAR(MAX) for SQL Server, TEXT/VARCHAR for other databases) instead of
 * using
 * database-specific JSON types like PostgreSQL's jsonb.
 * </p>
 * 
 * <h3>Usage:</h3>
 * 
 * <pre>
 * &#64;Convert(converter = MapToJsonConverter.class)
 * &#64;Column(name = "my_json_column", columnDefinition = "NVARCHAR(MAX)")
 * private Map&lt;String, String&gt; myJsonField;
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Converter
@Slf4j
public class MapToJsonConverter implements AttributeConverter<Map<String, String>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<Map<String, String>> MAP_TYPE_REF = new TypeReference<>() {
    };

    /**
     * Converts a Map to its JSON string representation for database storage.
     * 
     * @param attribute the Map to convert
     * @return JSON string representation, or null if the map is null
     */
    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error converting Map to JSON string: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to convert Map to JSON string", e);
        }
    }

    /**
     * Converts a JSON string from the database back to a Map.
     * 
     * @param dbData the JSON string from database
     * @return Map representation, or empty map if the string is null/empty
     */
    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(dbData, MAP_TYPE_REF);
        } catch (IOException e) {
            log.error("Error converting JSON string to Map: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to convert JSON string to Map", e);
        }
    }
}

package com.mmva.newsapp.infrastructure.common.exception;

import java.util.Map;

/**
 * Registry interface for constraint mapping configuration.
 * 
 * Allows features to register their constraint mappings without modifying the core mapper.
 * ARCHITECTURE: Each domain feature implements this to provide its constraint context.
 * 
 * PRINCIPLE: Feature ownership - news registrations in news feature, admin registrations in admin feature, etc.
 */
public interface ConstraintRegistry {

    /**
     * Get a unique identifier for this registry.
     * Used for logging and debugging.
     * Example: "news", "admin-user", "newsletter"
     * 
     * @return Registry identifier
     */
    String getRegistryId();

    /**
     * Constraint name to field name mappings.
     * Example: "news_title_en_uk" → "news_title_en"
     * 
     * @return Map of constraint names to field names
     */
    Map<String, String> getConstraintToFieldMappings();

    /**
     * Column name to field name mappings (for direct extraction).
     * Example: "news_title_en" → "news_title_en"
     * 
     * @return Map of column names to field names
     */
    Map<String, String> getColumnToFieldMappings();

    /**
     * Human-friendly field labels for error messages.
     * Example: "news_title_en" → "English title"
     * 
     * @return Map of field names to human-friendly labels
     */
    Map<String, String> getFieldLabels();

    /**
     * Entity context for generating uniform error messages.
     * Example: "news_title_en" → EntityContext("article", "title")
     * 
     * This context is used to generate messages like:
     * "An article with this English title already exists. Please use a different title."
     * 
     * @return Map of field names to entity context objects
     */
    Map<String, ConstraintViolationMapper.EntityContext> getEntityContextMappings();
}

package com.mmva.newsapp.infrastructure.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps database constraint violations to business field names.
 * INDUSTRY STANDARD: Extract field information from constraint violations
 * and treat like validation errors (display inline with specific fields).
 * 
 * ARCHITECTURE: Refactored to use ConstraintRegistry pattern for feature-specific configurations.
 * This mapper now acts as a coordinator that delegates to feature-specific registries.
 * Each feature provides its own constraint mappings without modifying this central class.
 * 
 * This is EXTENSIBLE: Add new features by creating a ConstraintRegistry implementation in that feature's config package.
 */
@Slf4j
@Component
public class ConstraintViolationMapper {

    private final ConstraintRegistryManager registryManager;
    // Legacy static maps - kept for backward compatibility during migration
    // TODO: Remove after all static constraints are migrated to registries
    private static final Map<String, String> CONSTRAINT_TO_FIELD = new HashMap<>();
    private static final Map<String, String> COLUMN_TO_FIELD = new HashMap<>();
    private static final Map<String, String> FIELD_LABELS = new HashMap<>();
    private static final Map<String, EntityContext> ENTITY_CONTEXT = new HashMap<>();

    public ConstraintViolationMapper(ConstraintRegistryManager registryManager) {
        this.registryManager = registryManager;
        log.info("✅ ConstraintViolationMapper initialized with registry manager");
        log.info("   Registered features: {}", registryManager.getRegisteredFeatures());
    }

    static {
        // NEWS constraints
        // Title uniqueness enforced at BOTH layers:
        // 1. Application layer (service validation) - user-friendly errors
        // 2. Database layer (unique constraints) - data integrity + race condition prevention
        // CONSTRAINT_TO_FIELD.put("uks1u5dxsooyrldwsltg8ms53j3", "news_slug"); // REMOVED (no longer unique)
        // CONSTRAINT_TO_FIELD.put("UQ_news_slug", "news_slug"); // REMOVED (no longer unique)

        // NEWS CATEGORY constraints
        CONSTRAINT_TO_FIELD.put("news_categories_slug_uk", "news_categories_slug");

        // ADMIN USER constraints
        CONSTRAINT_TO_FIELD.put("admin_users_username_uk", "admin_users_username");
        CONSTRAINT_TO_FIELD.put("admin_users_email_uk", "admin_users_email");

        // NEWS SOURCE AGENCY constraints
        CONSTRAINT_TO_FIELD.put("agency_code_uk", "agency_code");

        // NEWSLETTER constraints
        CONSTRAINT_TO_FIELD.put("newsletter_subscriber_email_uk", "newsletter_subscriber_email");

        // RBAC constraints
        CONSTRAINT_TO_FIELD.put("role_name_uk", "role_name");
        CONSTRAINT_TO_FIELD.put("permission_name_uk", "permission_name");

        // MONETIZATION constraints
        CONSTRAINT_TO_FIELD.put("invoice_number_uk", "invoice_number");

        // PUSH constraints
        CONSTRAINT_TO_FIELD.put("push_device_fcm_token_uk", "fcm_token");
        CONSTRAINT_TO_FIELD.put("push_notification_idempotency_key_uk", "idempotency_key");

        // Column to field mappings (for direct column name extraction)
        // Enterprise-grade: Both application and database constraints for title uniqueness
        COLUMN_TO_FIELD.put("news_title_en", "news_title_en");
        COLUMN_TO_FIELD.put("news_title_es", "news_title_es");
        COLUMN_TO_FIELD.put("news_categories_slug", "news_categories_slug");
        COLUMN_TO_FIELD.put("admin_users_username", "admin_users_username");
        COLUMN_TO_FIELD.put("admin_users_email", "admin_users_email");
        COLUMN_TO_FIELD.put("agency_code", "agency_code");
        COLUMN_TO_FIELD.put("newsletter_subscriber_email", "newsletter_subscriber_email");
        COLUMN_TO_FIELD.put("role_name", "role_name");
        COLUMN_TO_FIELD.put("permission_name", "permission_name");
        COLUMN_TO_FIELD.put("invoice_number", "invoice_number");
        COLUMN_TO_FIELD.put("fcm_token", "fcm_token");
        COLUMN_TO_FIELD.put("idempotency_key", "idempotency_key");

        // Human-friendly field labels for error messages
        // Used to display professional, understandable error messages to users
        // NEWS fields
        FIELD_LABELS.put("news_title_en", "English title");
        FIELD_LABELS.put("news_title_es", "Spanish title");
        FIELD_LABELS.put("news_slug", "URL slug");
        FIELD_LABELS.put("news_categories_slug", "category slug");

        // ADMIN USER fields
        FIELD_LABELS.put("admin_users_username", "username");
        FIELD_LABELS.put("admin_users_email", "email");

        // NEWS SOURCE AGENCY fields
        FIELD_LABELS.put("agency_code", "agency code");

        // NEWSLETTER fields
        FIELD_LABELS.put("newsletter_subscriber_email", "subscriber email");

        // RBAC fields
        FIELD_LABELS.put("role_name", "role name");
        FIELD_LABELS.put("permission_name", "permission name");

        // MONETIZATION fields
        FIELD_LABELS.put("invoice_number", "invoice number");

        // PUSH fields
        FIELD_LABELS.put("fcm_token", "FCM token");
        FIELD_LABELS.put("idempotency_key", "idempotency key");
        COLUMN_TO_FIELD.put("idempotency_key", "idempotency_key");

        // Entity context mapping for uniform error messages
        // Structure: fieldName → EntityContext(entityType, actionWord)
        // Example: "An article with this English title already exists. Please use a different title."
        
        // NEWS entity
        ENTITY_CONTEXT.put("news_title_en", new EntityContext("article", "title"));
        ENTITY_CONTEXT.put("news_title_es", new EntityContext("article", "title"));
        ENTITY_CONTEXT.put("news_slug", new EntityContext("article", "slug"));
        ENTITY_CONTEXT.put("news_categories_slug", new EntityContext("article", "value"));

        // ADMIN USER entity
        ENTITY_CONTEXT.put("admin_users_username", new EntityContext("administrator", "value"));
        ENTITY_CONTEXT.put("admin_users_email", new EntityContext("administrator", "value"));

        // NEWS SOURCE AGENCY entity
        ENTITY_CONTEXT.put("agency_code", new EntityContext("agency", "value"));

        // NEWSLETTER entity
        ENTITY_CONTEXT.put("newsletter_subscriber_email", new EntityContext("subscription", "value"));

        // RBAC entities
        ENTITY_CONTEXT.put("role_name", new EntityContext("role", "value"));
        ENTITY_CONTEXT.put("permission_name", new EntityContext("permission", "value"));

        // MONETIZATION entity
        ENTITY_CONTEXT.put("invoice_number", new EntityContext("invoice", "value"));

        // PUSH entities
        ENTITY_CONTEXT.put("fcm_token", new EntityContext("device", "value"));
        ENTITY_CONTEXT.put("idempotency_key", new EntityContext("notification", "value"));
    }

    /**
     * Extract field name from constraint violation error message.
     * 
     * EXTRACTION PRIORITY:
     * 1. Extract from registry manager mappings (feature-specific + legacy)
     * 2. Extract column name from error message
     * 3. Generic fallback from SQL error message
     * 
     * @param errorMessage Database constraint violation error message
     * @return Field name in snake_case or 'unknown_field' if cannot extract
     */
    public String extractFieldFromError(String errorMessage) {
        if (errorMessage == null) {
            log.warn("Error message is null, returning unknown_field");
            return "unknown_field";
        }
        
        log.debug("Extracting field from error message: {}", errorMessage);

        // Try 1: Match constraint name from error using registry manager
        // Pattern: constraint [«constraint_name»]
        Pattern constraintPattern = Pattern.compile("«([^»]+)»");
        Matcher constraintMatcher = constraintPattern.matcher(errorMessage);
        if (constraintMatcher.find()) {
            String constraintName = constraintMatcher.group(1);
            log.debug("Found constraint name: {}", constraintName);
            
            // Check registry manager first (feature-specific implementations)
            Map<String, String> registryConstraints = registryManager.getAggregatedConstraintToField();
            if (registryConstraints.containsKey(constraintName)) {
                String field = registryConstraints.get(constraintName);
                log.debug("✅ Constraint {} maps to field {} (from registry)", constraintName, field);
                return field;
            } else {
                log.debug("❌ Constraint {} not found in registry (size: {})", constraintName, registryConstraints.size());
            }
            
            // Fallback to static map (legacy, during migration)
            if (CONSTRAINT_TO_FIELD.containsKey(constraintName)) {
                String field = CONSTRAINT_TO_FIELD.get(constraintName);
                log.debug("✅ Constraint {} maps to field {} (from legacy)", constraintName, field);
                return field;
            }
            
            log.debug("⚠️ Constraint {} not found in mappings, will try alternate methods", constraintName);
        }

        // Try 2: Extract column name from error using registry manager
        // Pattern: (field_name)=
        Pattern columnPattern = Pattern.compile("\\((\\w+)\\)=");
        Matcher columnMatcher = columnPattern.matcher(errorMessage);
        if (columnMatcher.find()) {
            String columnName = columnMatcher.group(1);
            log.debug("Found column name: {}", columnName);
            
            // Check registry manager first (feature-specific implementations)
            Map<String, String> registryColumns = registryManager.getAggregatedColumnToField();
            if (registryColumns.containsKey(columnName)) {
                String field = registryColumns.get(columnName);
                log.debug("✅ Column {} maps to field {} (from registry)", columnName, field);
                return field;
            } else {
                log.debug("❌ Column {} not found in registry (size: {})", columnName, registryColumns.size());
            }
            
            // Fallback to static map (legacy, during migration)
            if (COLUMN_TO_FIELD.containsKey(columnName)) {
                String field = COLUMN_TO_FIELD.get(columnName);
                log.debug("✅ Column {} maps to field {} (from legacy)", columnName, field);
                return field;
            }
            
            log.debug("⚠️ Column {} not found in mappings", columnName);
        }

        // Try 3: Generic extraction from SQL error
        if (errorMessage.contains("(") && errorMessage.contains(")")) {
            Pattern sqlPattern = Pattern.compile("\\((.*?)\\)");
            Matcher sqlMatcher = sqlPattern.matcher(errorMessage);
            if (sqlMatcher.find()) {
                String extracted = sqlMatcher.group(1);
                log.debug("Extracted field {} from SQL error (generic fallback)", extracted);
                return extracted;
            }
        }

        log.warn("Could not extract field from constraint error: {}", errorMessage);
        return "unknown_field";
    }

    /**
     * Generate user-friendly message for constraint violation
     * 
     * ENTERPRISE-GRADE: Generates context-aware messages that match application layer
     * Uses registry manager to get entity context from feature-specific implementations.
     * 
     * Supports multiple database locales (English, Spanish, etc.)
     * 
     * Examples:
     * - "An article with this English title already exists. Please use a different title."
     * - "An administrator with this email already exists. Please use a different value."
     * - "A subscription with this email already exists. Please use a different value."
     * 
     * This ensures consistency across both application and database validation layers.
     */
    public String generateUserMessage(String field, String errorMessage) {
        String fieldLabel = this.humanizeField(field);
        
        // DEBUG: Log what we received for troubleshooting
        log.debug("generateUserMessage called: field={}, errorMessage={}", field, errorMessage);

        // Check if this is a uniqueness/duplicate constraint violation
        // Database error messages may vary by locale:
        // - English: "duplicate", "unique", "Constraint", "violated"
        // - Spanish: "llave duplicada", "unicidad", "restricción"
        // - Portuguese: "chave duplicada", "restrição de integridade"
        if (errorMessage == null) {
            log.debug("Error message is null");
            return "This value violates a constraint. Please choose a different value.";
        }
        
        String lowerMessage = errorMessage.toLowerCase();
        boolean isUniqueConstraintViolation = 
            lowerMessage.contains("duplicate") ||    // English
            lowerMessage.contains("unique") ||       // English
            lowerMessage.contains("llave duplicada") || // Spanish
            lowerMessage.contains("unicidad") ||     // Spanish
            lowerMessage.contains("restricción") ||  // Spanish
            lowerMessage.contains("chave duplicada") || // Portuguese
            (lowerMessage.contains("constraint") && lowerMessage.contains("violat")); // Generic
        
        if (isUniqueConstraintViolation) {
            log.debug("✅ Detected uniqueness constraint violation for field: {}", field);
            
            // Check registry manager first (feature-specific implementations)
            Map<String, EntityContext> registryContext = registryManager.getAggregatedEntityContext();
            if (registryContext.containsKey(field)) {
                EntityContext context = registryContext.get(field);
                String message = String.format(
                    "An %s with this %s already exists. Please use a different %s.",
                    context.entityType,
                    fieldLabel,
                    context.actionWord
                );
                log.debug("Generated context-aware message from registry: {}", message);
                return message;
            }
            
            // Fallback to legacy static map (during migration)
            if (ENTITY_CONTEXT.containsKey(field)) {
                EntityContext context = ENTITY_CONTEXT.get(field);
                String message = String.format(
                    "An %s with this %s already exists. Please use a different %s.",
                    context.entityType,
                    fieldLabel,
                    context.actionWord
                );
                log.debug("Generated context-aware message from legacy map: {}", message);
                return message;
            }
            
            // Final fallback to generic message
            String message = String.format("A record with this %s already exists. Please use a different value.", fieldLabel);
            log.debug("Using generic uniqueness message (no context found): {}", message);
            return message;
        }

        log.debug("❌ Not a uniqueness constraint violation, using generic message");
        return "This value violates a constraint. Please choose a different value.";
    }

    /**
     * Convert field name to human-readable label
     * PRIORITY:
     * 1. Check registry manager labels (feature-specific)
     * 2. Check legacy static labels
     * 3. Fall back to generic humanization (snake_case → Title Case)
     * 
     * Example outputs:
     * - "news_title_en" → "English title" (from registry)
     * - "admin_users_email" → "email" (from registry)
     * - "some_unknown_field" → "Some Unknown Field" (generic conversion)
     */
    private String humanizeField(String field) {
        // Step 1: Check registry manager labels (feature-specific)
        Map<String, String> registryLabels = registryManager.getAggregatedFieldLabels();
        if (registryLabels.containsKey(field)) {
            return registryLabels.get(field);
        }

        // Step 2: Check explicit labels from legacy static map (during migration)
        if (FIELD_LABELS.containsKey(field)) {
            return FIELD_LABELS.get(field);
        }

        // Step 3: Fall back to generic humanization
        String result = field
                .replaceAll("_", " ")
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("(?i)id$", "ID")
                .replaceAll("(?i)url$", "URL")
                .trim();

        // Capitalize first character
        if (!result.isEmpty()) {
            result = result.substring(0, 1).toUpperCase() + result.substring(1);
        }
        return result;
    }

    /**
     * Helper class to hold entity context for error message generation
     * Ensures consistent, professional error messages across application and database layers
     * 
     * PUBLIC: Accessible to ConstraintRegistry implementations for feature-specific mappings
     */
    public static class EntityContext {
        public final String entityType;  // e.g., "article", "administrator", "subscription"
        public final String actionWord;  // e.g., "title", "value", "email"

        public EntityContext(String entityType, String actionWord) {
            this.entityType = entityType;
            this.actionWord = actionWord;
        }
    }
}

package com.mmva.newsapp.infrastructure.common.util;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

/**
 * Utility class for input sanitization to prevent XSS and other injection
 * attacks.
 */
@Component
public class InputSanitizer {

    // Pattern to match potentially dangerous HTML/script tags
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
            "<script[^>]*>.*?</script>|javascript:|on\\w+\\s*=",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    // Pattern to match SQL injection attempts
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(--|;|'|\"\\s*(or|and)\\s*\"?\\d|union\\s+select|drop\\s+table)",
            Pattern.CASE_INSENSITIVE);

    /**
     * Sanitizes input string by escaping HTML entities.
     * This prevents XSS attacks by converting special characters to their HTML
     * entity equivalents.
     *
     * @param input the raw input string
     * @return sanitized string with HTML entities escaped
     */
    public String sanitizeHtml(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        return HtmlUtils.htmlEscape(input);
    }

    /**
     * Removes potentially dangerous script tags and JavaScript from input.
     *
     * @param input the raw input string
     * @return cleaned string with script content removed
     */
    public String removeScripts(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        return SCRIPT_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Full sanitization: removes scripts and escapes HTML.
     *
     * @param input the raw input string
     * @return fully sanitized string
     */
    public String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        String cleaned = removeScripts(input);
        return sanitizeHtml(cleaned);
    }

    /**
     * Sanitizes input for use in database queries (basic protection).
     * Note: Always use parameterized queries as the primary defense.
     *
     * @param input the raw input string
     * @return sanitized string
     */
    public String sanitizeForQuery(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        // Check for potential SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            throw new IllegalArgumentException("Invalid characters in input");
        }
        return input.trim();
    }

    /**
     * Validates and sanitizes a name field (no special characters except space,
     * hyphen, apostrophe).
     *
     * @param name the name to validate
     * @return sanitized name
     */
    public String sanitizeName(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        // Remove anything that's not a letter, space, hyphen, or apostrophe
        String sanitized = name.replaceAll("[^\\p{L}\\s'-]", "").trim();
        return sanitizeHtml(sanitized);
    }

    /**
     * Validates and sanitizes a phone number (digits, plus, hyphen, parentheses,
     * spaces only).
     *
     * @param phone the phone number to validate
     * @return sanitized phone number
     */
    public String sanitizePhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        // Keep only digits, plus, hyphen, parentheses, and spaces
        return phone.replaceAll("[^\\d+\\-()\\s]", "").trim();
    }

    /**
     * Sanitizes a URL to prevent javascript: and data: scheme injections.
     *
     * @param url the URL to sanitize
     * @return sanitized URL or null if dangerous
     */
    public String sanitizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        String lowerUrl = url.toLowerCase().trim();
        // Block dangerous URL schemes
        if (lowerUrl.startsWith("javascript:") ||
                lowerUrl.startsWith("data:") ||
                lowerUrl.startsWith("vbscript:")) {
            return null;
        }
        return sanitizeHtml(url);
    }
}

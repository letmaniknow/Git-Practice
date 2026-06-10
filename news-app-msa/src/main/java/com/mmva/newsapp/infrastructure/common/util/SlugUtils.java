package com.mmva.newsapp.infrastructure.common.util;

import java.util.UUID;

/**
 * Utility class for generating URL-friendly slugs from text.
 */
public final class SlugUtils {

    private SlugUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Generates a URL-friendly slug from the given input text.
     * 
     * Rules:
     * - Converts to lowercase
     * - Removes special characters (keeps alphanumeric and hyphens)
     * - Replaces spaces with hyphens
     * - Collapses multiple hyphens into one
     * - Limits length to 100 characters
     * - Falls back to UUID if input is null/empty
     * 
     * @param input the text to slugify
     * @return a URL-friendly slug
     */
    public static String slugify(String input) {
        if (input == null || input.isBlank()) {
            return UUID.randomUUID().toString();
        }

        String slug = input.trim()
                .toLowerCase()
                .replaceAll("[^\\p{L}0-9\\s-]", "") // Allow all Unicode letters, digits, spaces, hyphens
                .replaceAll("\\s+", "-") // Replace spaces with hyphens
                .replaceAll("-+", "-") // Collapse multiple hyphens
                .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens

        if (slug.isEmpty()) {
            return UUID.randomUUID().toString();
        }

        return slug.length() > 100 ? slug.substring(0, 100) : slug;
    }
}

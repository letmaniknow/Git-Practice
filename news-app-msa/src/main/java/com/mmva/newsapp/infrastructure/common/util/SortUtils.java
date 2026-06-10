package com.mmva.newsapp.infrastructure.common.util;

import org.springframework.data.domain.Sort;

/**
 * Utility class for sorting operations.
 */
public final class SortUtils {

    private SortUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Parses a sort string (e.g., "createdAt,desc") to a Sort.Order.
     * 
     * @param sort the sort string in format "property,direction" (e.g.,
     *             "createdAt,desc")
     * @return Sort.Order object, defaults to descending by createdAt if input is
     *         null or empty
     */
    public static Sort.Order parseSort(String sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.Order.desc("createdAt");
        }
        String[] parts = sort.split(",");
        String property = parts[0];
        boolean desc = parts.length > 1 && "desc".equalsIgnoreCase(parts[1]);
        return desc ? Sort.Order.desc(property) : Sort.Order.asc(property);
    }
}

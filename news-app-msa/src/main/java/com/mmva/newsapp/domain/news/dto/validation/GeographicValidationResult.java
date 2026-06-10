package com.mmva.newsapp.domain.news.dto.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data transfer object containing geographic validation results.
 *
 * <p>
 * Encapsulates validation results for geographic data including coordinate
 * validation, location verification, and geographic consistency checks.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeographicValidationResult {

    /**
     * Whether the geographic data is valid.
     */
    private boolean isValid;

    /**
     * List of geographic validation errors.
     */
    private List<String> errors;

    /**
     * List of geographic validation warnings.
     */
    private List<String> warnings;

    /**
     * Normalized location name.
     */
    private String normalizedLocationName;

    /**
     * Normalized country name.
     */
    private String normalizedCountry;

    /**
     * Normalized state/province name.
     */
    private String normalizedState;

    /**
     * Whether coordinates are within the specified country.
     */
    private boolean coordinatesWithinCountry;

    /**
     * Geographic validation confidence score (0-100).
     */
    private double confidenceScore;
}